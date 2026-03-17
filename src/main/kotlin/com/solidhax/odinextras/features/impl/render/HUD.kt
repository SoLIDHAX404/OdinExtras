package com.solidhax.odinextras.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.skyblock.LocationUtils

enum class HudType {
    POTION_EFFECT,
    TOOLTIP,
    VIGNETTE
}

object HUD : Module(
    name = "HUD",
    description = "Make the vanilla HUD elements more customizable."
) {
    private val hidePotionEffects by BooleanSetting("Hide Potion Effects", false, desc = "Whether or not to hide potion effects.")
    private val hideTooltip by BooleanSetting("Hide Tooltip", false, desc = "Whether or not to hide the item switch tooltip.")
    private val hideVignette by BooleanSetting("Hide Vignette", false, desc = "Whether or not to hide the vignette.")

    @JvmStatic
    fun shouldCancelHud(type: HudType): Boolean {
        if (!enabled || !LocationUtils.isInSkyblock) return false
        return when (type) {
            HudType.POTION_EFFECT -> hidePotionEffects
            HudType.TOOLTIP -> hideTooltip
            HudType.VIGNETTE -> hideVignette
        }
    }
}