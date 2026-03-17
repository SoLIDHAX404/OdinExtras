package com.solidhax.odinextras.features.impl.skyblock

import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.render.drawCustomBeacon
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.skyblock.Island
import com.odtheking.odin.utils.skyblock.LocationUtils
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.AABB

enum class CorpseType {
    LAPIS,
    TUNGSTEN,
    UMBER,
    VANGUARD
}

object CorpseFinder : Module(
    name = "Corpse Finder",
    description = "Finds and pings the location of corpses in a mineshaft."
) {

    private val corpses = mutableMapOf<CorpseType, MutableSet<Entity>>()

    init {
        on<TickEvent.End> {
            if(!enabled || !LocationUtils.isInSkyblock || LocationUtils.currentArea != Island.Mineshaft) return@on

            mc.level?.entitiesForRendering()?.forEach { e ->
                val entity = e ?: return@forEach
                if(!entity.isAlive || entity !is ArmorStand) return@forEach

                val entityName = entity.name.string
                if(entityName != "Armor Stand" || entity.isInvisible) return@forEach

                val helmetName = entity.getItemBySlot(EquipmentSlot.HEAD).customName?.string
                val type = when(helmetName) {
                    "Lapis Armor Helmet" -> CorpseType.LAPIS
                    "Mineral Helmet" -> CorpseType.TUNGSTEN
                    "Yog Helmet" -> CorpseType.UMBER
                    "Vanguard Helmet" -> CorpseType.VANGUARD
                    else -> return@forEach
                }

                corpses.getOrPut(type) { mutableSetOf() }.add(entity)
            }
        }

        on<RenderEvent.Extract> {
            if(!enabled || !LocationUtils.isInSkyblock || LocationUtils.currentArea != Island.Mineshaft) return@on

            corpses.forEach { (type, entities) ->

                val color = when (type) {
                    CorpseType.LAPIS -> Color(0, 0, 255)
                    CorpseType.TUNGSTEN -> Color(255, 255, 255)
                    CorpseType.UMBER -> Color(181, 98, 34)
                    CorpseType.VANGUARD -> Color(242, 36, 184)
                }

                entities.forEach { entity ->
                    drawWireFrameBox(AABB(entity.blockPosition()), color)
                }
            }
        }

        on<WorldEvent.Load> {
            corpses.clear()
        }
    }

}