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
import com.solidhax.odinextras.api.MiningAPI
import com.solidhax.odinextras.api.MiningAPI.Commission
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object CommissionHelper : Module(
    name = "Commission Helper",
    description = "Various features to help with mining commissions.",
    category = Category.custom("Mining")
) {
    private val completedCommissionRegex = Regex("""(.+?) Commission Complete! Visit the King to claim your rewards!""")
    private val exampleCommissions: List<Commission> = listOf(
        Commission("Lava Springs Mithril", 45.0),
        Commission("Royal Mines Titanium", 100.0),
        Commission("Goblin Slayer", 13.0)
    )
    private var activeCommissions: List<Commission> = emptyList()

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

    private val activeCommissionsDisplay by BooleanSetting(
        "Commission Display",
        desc = "Shows a hud listing all active commissions and their progress."
    )

    private val hud by HUD("Commission HUD", "Active Commissions Overlay", false) { example ->
        if(!activeCommissionsDisplay) return@HUD 0 to 0
        activeCommissions = if(example) exampleCommissions else MiningAPI.commissions

        var width = 0
        var height = activeCommissions.size * mc.font.lineHeight
        activeCommissions.forEachIndexed { index, commission ->
            var commissionProgressText: String = if(commission.progress == 100.0) "DONE" else "${commission.progress}%"
            var commissionStatusColor: Color = progressToColor(commission.progress)
            val (textWidth, textHeight) = textDim("${commission.name}: $commissionProgressText", 0, 0 + (index * mc.font.lineHeight), commissionStatusColor)
            width = maxOf(width, textWidth)
        }

        width to height
    }.withDependency { activeCommissionsDisplay }

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