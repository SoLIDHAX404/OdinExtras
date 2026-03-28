package com.solidhax.odinextras.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.loreString
import net.minecraft.client.gui.GuiGraphics
import java.text.NumberFormat

object AuctionHouseHelper : Module(
    "Auction House Helper",
    description = "Useful features for the auction house"
) {

    private val soldOnlyRegex = Regex("""Sold for:\s*(?<price>[0-9,]+)\s*coins""")
    private val soldOrBinRegex = Regex("""(?:Sold for|Buy it now):\s*(?<price>[0-9,]+)\s*coins""")

    private val items = mutableMapOf<Int, Long>()

    private val numberFormatter = NumberFormat.getIntegerInstance()

    private val onlySold by BooleanSetting("Only Sold", desc = "Only count sold items for the value.")
    private val priceHud by HUD("Price Hud", "Price for all items in manage auction gui.") { example ->
        if(!example) return@HUD 0 to 0
        drawOverlay(true)
    }

    init {

        on<GuiEvent.DrawTooltip> {
            if (!enabled || screen.title.string != "Manage Auctions") return@on

            guiGraphics.pose().pushMatrix()
            val sf = mc.window.guiScale
            guiGraphics.pose().scale(1f / sf, 1f / sf)
            guiGraphics.pose().translate(priceHud.x.toFloat(), priceHud.y.toFloat())
            guiGraphics.pose().scale(priceHud.scale)

            guiGraphics.drawOverlay(false)

            guiGraphics.pose().popMatrix()
        }

        on<GuiEvent.RenderSlot> {
            if (!enabled || screen.title.string != "Manage Auctions" || slot.item == null) return@on

            val slotIndex = slot.index
            if (slotIndex in items) return@on

            val item = slot.item
            val loreLines = item.loreString

            val regex = if (onlySold) soldOnlyRegex else soldOrBinRegex
            for (line in loreLines) {
                val match = regex.find(line) ?: continue
                val priceString = match.groups["price"]?.value ?: continue
                val price = priceString.replace(",", "").toLongOrNull() ?: continue

                items[slotIndex] = price
                break
            }
        }
    }

    private fun GuiGraphics.drawOverlay(isEditing: Boolean): Pair<Int, Int> {
        val total = items.values.sum()
        val formatted = numberFormatter.format(total)
        val text = if (isEditing) "Value: 1,000,000 coins" else "Value: $formatted coins"

        val textWidth = mc.font.width(text)

        drawString(mc.font, text, 0, 0, Colors.MINECRAFT_GREEN.rgba)

        return textWidth to mc.font.lineHeight
    }
}