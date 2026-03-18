package com.solidhax.odinextras.features.impl.skyblock

import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.toFixed
import tech.thatgravyboat.skyblockapi.api.area.mining.Commission
import tech.thatgravyboat.skyblockapi.api.area.mining.CommissionArea
import tech.thatgravyboat.skyblockapi.api.area.mining.CommissionsAPI

object CommissionHelper : Module(
    name = "Commission Helper",
    description = "Various features to help with mining commissions."
) {
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

    private fun progressToColor(progress: Double): Color {
        return when {
            progress >= 75f -> Colors.MINECRAFT_GREEN
            progress >= 50f -> Colors.MINECRAFT_YELLOW
            progress >= 25f -> Colors.MINECRAFT_GOLD
            else -> Colors.MINECRAFT_RED
        }
    }
}