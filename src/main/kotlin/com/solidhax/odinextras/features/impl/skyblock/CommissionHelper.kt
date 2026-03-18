package com.solidhax.odinextras.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.loreString
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.toFixed
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.area.mining.Commission
import tech.thatgravyboat.skyblockapi.api.area.mining.CommissionArea
import tech.thatgravyboat.skyblockapi.api.area.mining.CommissionsAPI

object CommissionHelper : Module(
    name = "Commission Helper",
    description = "Various features to help with mining commissions."
) {
    private val highlightCompletedCommissions by BooleanSetting("Highlight Commissions", desc = "Highlights completed commissions in the Commissions GUI for quick claiming.")
    private val highlightColor by ColorSetting("Color", Colors.MINECRAFT_RED, allowAlpha = true, desc = "The color in which completed Commissions will be highlighted.").withDependency { highlightCompletedCommissions }

    private var activeCommissions: List<Commission> = mutableListOf()

    private val hud by HUD(name, "Active Commissions Overlay", false) { example ->
        activeCommissions = CommissionsAPI.commissions.filter { it.area == CommissionArea.currentArea }

        var width = 0
        var height = activeCommissions.size * mc.font.lineHeight
        activeCommissions.forEachIndexed { index, commission ->
            var commissionProgressPercentage: Double = commission.progress * 100.0
            var commissionStatusColor: Color = progressToColor(commissionProgressPercentage)
            val (textWidth, textHeight) = textDim("${commission.name}: ${commissionProgressPercentage.toFixed(1)}%", 0, 0 + (index * mc.font.lineHeight), commissionStatusColor)
            width = maxOf(width, textWidth)
        }

        width to height
    }

    init {
        on<GuiEvent.DrawSlot> {
            if(!highlightCompletedCommissions) return@on
            if(screen.title.equals("Commissions") || slot.item.item != Items.WRITABLE_BOOK) return@on
            if(!isCompletedCommission(slot.item)) return@on

            guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, highlightColor.rgba)
        }
    }

    private fun progressToColor(progress: Double): Color {
        return when {
            progress >= 75f -> Colors.MINECRAFT_GREEN
            progress >= 50f -> Colors.MINECRAFT_YELLOW
            progress >= 25f -> Colors.MINECRAFT_GOLD
            else -> Colors.MINECRAFT_RED
        }
    }

    private fun isCompletedCommission(item: ItemStack): Boolean {
        item.loreString.reversed().forEach { line ->
            if(line.contains("COMPLETED")) return true
        }

        return false
    }
}