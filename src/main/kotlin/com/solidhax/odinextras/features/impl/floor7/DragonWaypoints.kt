package com.solidhax.odinextras.features.impl.floor7

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.floor7.WitherDragonState
import com.odtheking.odin.features.impl.floor7.WitherDragons
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.drawBeaconBeam
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.render.drawTracer
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB

object DragonWaypoints : Module(name = "Dragon Waypoints", description = "Shows waypoints on where to stand for arrow stack/debuff.") {

    val arrowStackWaypoints: Map<Color, BlockPos> = mapOf(
        Colors.MINECRAFT_RED to BlockPos(59, 5, 67),
        Colors.MINECRAFT_GREEN to BlockPos(45, 5, 69),
        Colors.MINECRAFT_AQUA to BlockPos(58, 6, 107),
        Colors.MINECRAFT_GOLD to BlockPos(65, 5, 80),
        Colors.MINECRAFT_DARK_PURPLE to BlockPos(39, 6, 100)
    )

    val debuffWaypoints: Map<Color, BlockPos> = mapOf(
        Colors.MINECRAFT_RED to BlockPos(25, 6, 57),
        Colors.MINECRAFT_GREEN to BlockPos(26, 6, 92),
        Colors.MINECRAFT_AQUA to BlockPos(86, 6, 96),
        Colors.MINECRAFT_GOLD to BlockPos(86, 6, 58),
        Colors.MINECRAFT_DARK_PURPLE to BlockPos(56, 8, 125)
    )

    private val shouldDrawBeaconBeam by BooleanSetting("Draw Beacon beam", false, "Whether to draw a beacon beam at the arrow stack/debuff position.")
    private val shouldDrawText by BooleanSetting("Draw Text", false, "Whether to draw a text at the arrow stack/debuff position.")
    private val shouldDrawTracer by BooleanSetting("Draw Tracer", false, "Whether to draw a tracer to the arrow stack/debuff position.")
    private val shouldDrawBox by BooleanSetting("Draw Box", false, "Whether to draw a box to the arrow stack/debuff position.")

    init {
        on<RenderEvent.Extract> {
            if(DungeonUtils.getF7Phase() != M7Phases.P5) return@on

            val priorityDragon = WitherDragons.priorityDragon

            priorityDragon?.let { dragon ->
                if(dragon.state != WitherDragonState.SPAWNING) return@on

                val dungeonClass = DungeonUtils.currentDungeonPlayer.clazz
                val shouldShowDebuffWaypoints = dungeonClass == DungeonClass.Mage || dungeonClass == DungeonClass.Tank || dungeonClass == DungeonClass.Healer

                val color = dragon.color
                val position = (if (shouldShowDebuffWaypoints) debuffWaypoints else arrowStackWaypoints)[color] ?: return@on
                val title = (if (shouldShowDebuffWaypoints) "Debuff" else "Arrow Stack")

                if(shouldDrawText)
                    drawText(title, position.center, 3f, false)

                if(shouldDrawBeaconBeam)
                    drawBeaconBeam(position, dragon.color)

                if(shouldDrawBox)
                    drawWireFrameBox(AABB(position), dragon.color)

                if(shouldDrawTracer)
                    mc.player?.let { drawTracer(position.center, color = color, true) }
            }
        }
    }
}