package com.solidhax.odinextras.utils

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.render.drawWireFrameBox
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.AABB
import kotlin.math.max

val Component.command: String?
    get() = this.style.clickEvent?.takeIf {
        it.action() == ClickEvent.Action.RUN_COMMAND
    }?.let { (it as ClickEvent.RunCommand).command }

fun RenderEvent.Extract.drawWaypoint(
    title: String,
    position: BlockPos,
    color: Color,
    increase: Boolean = true,
    distance: Boolean = true
) {
    val dist = mc.player?.blockPosition()?.distManhattan(position) ?: return

    drawWireFrameBox(AABB(position), color, depth = true)
    drawText(
        (if (distance) ("$title §r§f(§3${dist}m§f)") else title),
        position.center.addVec(y = 1.7),
        if (increase) max(1.5f, dist * 0.07f) else 3.0f,  // bigger text
        false
    )
}