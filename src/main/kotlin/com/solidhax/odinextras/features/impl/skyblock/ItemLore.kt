package com.solidhax.odinextras.features.impl.skyblock

import com.google.gson.JsonObject
import com.odtheking.mixin.accessors.AbstractContainerScreenAccessor
import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.customData
import com.odtheking.odin.utils.network.WebUtils.fetchJson
import com.solidhax.odinextras.events.RenderTooltipEvent
import kotlinx.coroutines.launch
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import kotlin.collections.mutableMapOf
import kotlin.jvm.optionals.getOrNull

object ItemLore : Module(
    name = "Item Lore",
    description = "Displays information about an item in its lore."
) {

    private val showNpcPrice by BooleanSetting("Show NPC Price", false, desc = "Whether or not the NPC Price should be displayed in the items lore.")
    private val showBinPrice by BooleanSetting("Show BIN Price", false, desc = "Whether or not the Lowest BIN Price should be displayed in the items lore.")
    private val showQuality by BooleanSetting("Show Quality", false, desc = "Whether or not the Quality should be displayed in the items lore.")

    var cachedLowestBINPrices: Map<String, Double> = emptyMap();
    var cachedNPCSellPrices: MutableMap<String, Double> = mutableMapOf();

    init {
        scope.launch {
            cachedLowestBINPrices = fetchJson<Map<String, Double>>("https://api.odtheking.com/lb/lowestbins").getOrElse { OdinMod.logger.error("Failed to fetch lowest bin prices for Item Lore module.", it); emptyMap() }

            val response = fetchJson<JsonObject>("https://api.hypixel.net/resources/skyblock/items").getOrNull() ?: run{ OdinMod.logger.error("Failed to fetch skyblock items list for Item Lore module."); return@launch }
            val items = response.getAsJsonArray("items") ?: return@launch

            items.asSequence().mapNotNull { it.asJsonObject }
                .forEach { item ->
                    val id = item.get("id")?.asString ?: return@forEach
                    val sellPrice = item.get("npc_sell_price")?.asDouble ?: return@forEach

                    cachedNPCSellPrices[id] = sellPrice
                }
        }

        on<RenderTooltipEvent> {
            val screen: Screen = mc.screen ?: return@on
            if(!(screen is InventoryScreen || screen is ContainerScreen)) return@on

            val accessor = screen as AbstractContainerScreenAccessor
            val hoveredSlot = accessor.hoveredSlot ?: return@on
            val item = hoveredSlot.item ?: return@on
            val data = item.customData

            displayPrices(data, lore)
            displayDungeonQuality(data, lore)
        }
    }

    private fun displayPrices(itemData: CompoundTag, lore: MutableList<ClientTooltipComponent>) {
        val id = itemData.getString("id").getOrNull() ?: return@displayPrices
        val itemBinPrice = cachedLowestBINPrices[id] ?: 0.0
        val itemNpcPrice = cachedNPCSellPrices[id] ?: 0.0

        val binPriceText = "%,.0f".format(itemBinPrice)
        val npcPriceText = "%,.0f".format(itemNpcPrice)

        val binPriceFormat = FormattedCharSequence.composite(
            FormattedCharSequence.forward("Lowest BIN: ", Style.EMPTY.withColor(ChatFormatting.GOLD)),
            FormattedCharSequence.forward(binPriceText, Style.EMPTY.withColor(ChatFormatting.AQUA)),
        )

        val npcPriceformat = FormattedCharSequence.composite(
            FormattedCharSequence.forward("NPC Value: ", Style.EMPTY.withColor(ChatFormatting.GOLD)),
            FormattedCharSequence.forward(npcPriceText, Style.EMPTY.withColor(ChatFormatting.AQUA)),
        )

        if(showNpcPrice)
            lore.addLast(ClientTooltipComponent.create(npcPriceformat))

        if(showBinPrice)
            lore.addLast(ClientTooltipComponent.create(binPriceFormat))
    }

    private fun displayDungeonQuality(itemData: CompoundTag, lore: MutableList<ClientTooltipComponent>) {
        val boost = itemData.getInt("baseStatBoostPercentage").getOrNull() ?: return@displayDungeonQuality
        val tier = itemData.getInt("item_tier").getOrNull() ?: return@displayDungeonQuality

        val format = FormattedCharSequence.composite(
            FormattedCharSequence.forward("Item Quality: ", Style.EMPTY.withColor(ChatFormatting.GOLD)),
            FormattedCharSequence.forward("$boost/50", Style.EMPTY.withColor(ChatFormatting.AQUA)),
            FormattedCharSequence.forward(" (Tier ", Style.EMPTY.withColor(ChatFormatting.GRAY)),
            FormattedCharSequence.forward(tier.toString(), Style.EMPTY.withColor(ChatFormatting.DARK_RED)),
            FormattedCharSequence.forward(")", Style.EMPTY.withColor(ChatFormatting.GRAY)),
        )

        if(showQuality)
            lore.addLast(ClientTooltipComponent.create(format))
    }
}