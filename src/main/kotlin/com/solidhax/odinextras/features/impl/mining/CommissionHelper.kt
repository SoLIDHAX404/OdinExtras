package com.solidhax.odinextras.features.impl.mining

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.Island
import com.odtheking.odin.utils.skyblock.LocationUtils
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.area.mining.Commission
import tech.thatgravyboat.skyblockapi.api.area.mining.CommissionArea
import tech.thatgravyboat.skyblockapi.api.area.mining.CommissionsAPI

object CommissionHelper : Module(
    name = "Commission Helper",
    description = "Various features to help with mining commissions.",
    category = Category.custom("Mining")
) {
    private val highlightCompletedCommissions by BooleanSetting(
        "Highlight Commissions",
        desc = "Highlights completed commissions in the commissions GUI for quick claiming."
    )

    private val highlightColor by ColorSetting(
        "Color",
        Colors.MINECRAFT_RED,
        allowAlpha = true,
        desc = "The color in which completed commissions will be highlighted."
    ).withDependency { highlightCompletedCommissions }

    private val completedCommissionAlert by BooleanSetting(
        "Commission Alert",
        desc = "Alerts the player if he completed a commission."
    )

    private val completedCommissionRegex = Regex("""(.+?) Commission Complete! Visit the King to claim your rewards!""")
    private val exampleCommissions: List<Commission> = listOf(
        Commission("Lava Springs Mithril", CommissionArea.DWARVEN_MINES, 0.45f),
        Commission("Royal Mines Titanium", CommissionArea.DWARVEN_MINES, 1.0f),
        Commission("Goblin Slayer", CommissionArea.DWARVEN_MINES, 0.13f)
    )
    private var activeCommissions: List<Commission> = mutableListOf()

    private val hud by HUD(name, "Active Commissions Overlay", false) { example ->
        activeCommissions = if(example) exampleCommissions else CommissionsAPI.commissions.filter { it.area == CommissionArea.Companion.currentArea }

        var width = 0
        var height = activeCommissions.size * mc.font.lineHeight
        activeCommissions.forEachIndexed { index, commission ->
            var commissionProgressPercentage: Double = commission.progress * 100.0
            var commissionProgressText: String = if(commissionProgressPercentage == 100.0) "DONE" else "${commissionProgressPercentage.toFixed(1)}%"
            var commissionStatusColor: Color = progressToColor(commissionProgressPercentage)
            val (textWidth, textHeight) = textDim("${commission.name}: $commissionProgressText", 0, 0 + (index * mc.font.lineHeight), commissionStatusColor)
            width = maxOf(width, textWidth)
        }

        width to height
    }

    init {
        on<GuiEvent.DrawSlot> {
            if(!enabled || !highlightCompletedCommissions) return@on

            if(!LocationUtils.isInSkyblock) return@on
            if(!LocationUtils.isCurrentArea(Island.DwarvenMines, Island.CrystalHollows, Island.Mineshaft)) return@on
            if(!screen.title.string.contains("Commissions") || slot.item.item != Items.WRITABLE_BOOK) return@on
            if(!isCompletedCommission(slot.item)) return@on

            guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, highlightColor.rgba)
        }

        on<ChatPacketEvent> {
            if(!enabled || !completedCommissionAlert) return@on

            if(!LocationUtils.isInSkyblock) return@on
            if(!LocationUtils.isCurrentArea(Island.DwarvenMines, Island.CrystalHollows, Island.Mineshaft)) return@on

            if(completedCommissionRegex.containsMatchIn(value)) {
                alert("COMMISSION COMPLETED")
            }
        }
    }

    private fun progressToColor(progress: Double): Color {
        return when {
            progress >= 75.0 -> Colors.MINECRAFT_GREEN
            progress >= 50.0 -> Colors.MINECRAFT_YELLOW
            progress >= 25.0 -> Colors.MINECRAFT_GOLD
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