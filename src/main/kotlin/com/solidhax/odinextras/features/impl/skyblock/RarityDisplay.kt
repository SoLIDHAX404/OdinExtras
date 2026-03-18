package com.solidhax.odinextras.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.getSkyblockRarity
import com.odtheking.odin.utils.loreString
import com.solidhax.odinextras.events.PlayerGuiEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier


object RarityDisplay : Module(
    name = "Rarity Display",
    description = "Displays a colored background depending on the Item Rarity."
) {

    val RARITY_TEXTURES: Array<Identifier?> = arrayOf<Identifier?>(
        Identifier.fromNamespaceAndPath("odinextras", "rarity"),
        Identifier.fromNamespaceAndPath("odinextras", "rarity2"),
        Identifier.fromNamespaceAndPath("odinextras", "rarity3"),
        Identifier.fromNamespaceAndPath("odinextras", "rarity4")
    )

    private val itemBackgroundType by SelectorSetting("Type", "Circle", listOf("Circle", "Square", "Square Outline", "Outline"), desc = "The type of background drawn behind an item.")
    private val itemBackgroundOpacity by NumberSetting("Opacity", 100f, 0f, 100f, desc = "The opacity of the background.")

    init {
        on<GuiEvent.DrawSlot> {
            if(!enabled) return@on

            val itemInSlot = slot.item
            if (itemInSlot.isEmpty) return@on
            val itemRarity = getSkyblockRarity(itemInSlot.loreString) ?: return@on

            val itemRarityBackground = getSelectedItemBackgroundTexture() ?: return@on;
            val itemRarityColor = itemRarity.color.withAlpha(itemBackgroundOpacity / 100f);

            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, itemRarityBackground, slot.x, slot.y, 16, 16, itemRarityColor.rgba)
        }

        on<PlayerGuiEvent.DrawSlot> {
            if(!enabled) return@on

            val itemRarity = getSkyblockRarity(item.loreString) ?: return@on

            val itemRarityBackground = getSelectedItemBackgroundTexture() ?: return@on;
            val itemRarityColor = itemRarity.color.withAlpha(itemBackgroundOpacity / 100f);

            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, itemRarityBackground, x, y, 16, 16, itemRarityColor.rgba)
        }
    }

    private fun getSelectedItemBackgroundTexture(): Identifier? {
        if (itemBackgroundType !in RARITY_TEXTURES.indices) return null
        return RARITY_TEXTURES[itemBackgroundType]
    }
}