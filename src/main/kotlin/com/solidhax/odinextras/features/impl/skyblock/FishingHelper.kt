package com.solidhax.odinextras.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.render.drawText
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.Projectile

object FishingHelper : Module(
    name = "Fishing Helper",
    description = "Various features to help with the fishing skill"
) {
    private val showFishingHookLifetime by BooleanSetting("Bobber Lifetime", desc = "Displays the time the fishing bobber has been active at its position.")

    private var fishingHookEntity: Projectile? = null
    private var fishingHookLifetimeTicks: Int = 0

    init {
        onReceive<ClientboundAddEntityPacket> {
            if(type != EntityType.FISHING_BOBBER) return@onReceive

            schedule(1) {
                var entity: Projectile = mc.level?.getEntity(id) as Projectile
                if(entity.owner != mc.player) return@schedule

                fishingHookEntity = entity
            }
        }

        onReceive<ClientboundRemoveEntitiesPacket> {
            if(fishingHookEntity == null) return@onReceive
            if(!entityIds.contains(fishingHookEntity!!.id)) return@onReceive

            fishingHookEntity = null
            fishingHookLifetimeTicks = 0
        }

        on<TickEvent.Server> {
            if(fishingHookEntity == null) return@on

            fishingHookLifetimeTicks++
        }

        on<RenderEvent.Extract> {
            if(!showFishingHookLifetime) return@on
            if(fishingHookEntity == null) return@on

            val fishingHookLifetimeSeconds: Double = fishingHookLifetimeTicks / 20.0
            val formattedTime = String.format("%.2f", fishingHookLifetimeSeconds)
            drawText("§6${formattedTime}s", fishingHookEntity!!.position(), 1f, false)
        }
    }
}