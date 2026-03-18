package com.solidhax.odinextras.features.impl.skyblock

import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.render.drawTracer
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.Island
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.solidhax.odinextras.utils.command
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.arrow.Arrow

object Trapper : Module(
    name = "Trapper",
    description = "Trevor the Trapper related features."
) {
    private val showBoundingBox by BooleanSetting("Bounding Box", false, desc = "Draws a bounding box around the found entity.")
    private val showTracer by BooleanSetting("Tracer", false, desc = "Draws a tracer to the found entity.")

    private val showTitle by BooleanSetting("Mob Title", false, desc = "Shows a title for when a valid mob is found.")
    private val autoAccept by BooleanSetting("Auto Accept", false, desc = "Auto accepts the trapper quest.")
    private val autoAcceptDelay by NumberSetting("Delay", 10, 0, 40, desc = "Delays the click of the yes chat prompt (Randomized by default)").withDependency { autoAccept }
    private val warpToTrapperKeybind by KeybindSetting("Warp to Trapper", InputConstants.UNKNOWN, desc = "Keybind to warp to trapper after killing the mob.").onPress {
        if(!enabled || !LocationUtils.isInSkyblock || LocationUtils.currentArea != Island.FarmingIsland) return@onPress
        sendCommand("warp trapper")
    }

    private val soundDropdown by DropdownSetting("Sounds")
    private val playSound by BooleanSetting("Play Sound", false, desc = "Plays a sound for when a valid entity is found.").withDependency { soundDropdown }
    private val soundSetting = createSoundSettings("Mob found sound", "block.note_block.chime") { soundDropdown && playSound }

    private val hud by HUD(name, "Displays the trapper cooldown in the HUD.", false) { example ->
        when {
            example -> "Ready!"
            !LocationUtils.isInSkyblock || LocationUtils.currentArea != Island.FarmingIsland -> null
            trapperTimer > 0 -> "${(trapperTimer / 20f).toFixed()}s"
            else -> "Ready!"
        }?.let { text ->
            textDim("§aTrapper: $text", 0, 0, Colors.WHITE)
        } ?: (0 to 0)
    }

    private val trapperStartRegex = Regex("""You can find your (.+?) animal near the (?<area>.+?)(?:\.|$)""")
    private val trapperClickOptionPattern = Regex("Click an option: \\[YES] - \\[NO]")
    private var trapperTimer = -1
    private var trapperPromptTimer = 0
    private var lastChatPrompt = ""

    private val trapperMobs = hashSetOf("Trackable", "Untrackable", "Undetected", "Endangered", "Elusive")
    private val entities = mutableSetOf<Entity>()

    init {

        on<ChatPacketEvent> {
            if(!enabled) return@on

            val trapperStart = trapperStartRegex.find(value) != null
            val trapperOptions = trapperClickOptionPattern.find(value) != null

            when {
                trapperStart -> trapperTimer = 300 //ticks 15 * 20 = 300
                trapperOptions && autoAccept -> {
                    for(sibling in component.siblings) {
                        val clickEvent = sibling.command ?: continue

                        if(clickEvent.contains("YES")) {
                            trapperPromptTimer = autoAcceptDelay + (1..15).random()
                            lastChatPrompt = clickEvent.substringAfter(" ")
                        }
                    }
                }
            }
        }

        on<TickEvent.End> {
            if(!enabled || !LocationUtils.isInSkyblock || LocationUtils.currentArea != Island.FarmingIsland) return@on

            mc.level?.entitiesForRendering()?.forEach { e ->
                val entity = e ?: return@forEach
                if(!entity.isAlive || entity !is ArmorStand) return@forEach

                val entityName = entity.name.string
                if(entityName == "Armor Stand" || !entity.isInvisible) return@forEach
                if(!trapperMobs.any{ it in entityName }) return@forEach

                mc.level?.getEntities(entity, entity.boundingBox.inflate(0.5).move(0.0, -1.0, 0.0)) { isValidEntity(it) }?.firstOrNull()?.let {
                    if(!entities.add(it)) return@forEach

                    if(showTitle) alert("§cTrapper Mob Found!", false)
                    if(playSound) playSoundSettings(soundSetting())
                }
            }

            entities.removeIf { entity -> !entity.isAlive }
        }

        on<TickEvent.Server> {
            if(!enabled) return@on

            if(trapperTimer > 0) trapperTimer--

            if (trapperPromptTimer > 0) {
                trapperPromptTimer--

                if (autoAccept && trapperPromptTimer == 0 && lastChatPrompt.isNotEmpty()) {
                    sendCommand("chatprompt $lastChatPrompt")
                }
            }
        }

        on<RenderEvent.Extract> {
            if(!enabled || !LocationUtils.isInSkyblock || LocationUtils.currentArea != Island.FarmingIsland) return@on

            entities.forEach { entity ->
                if(!entity.isAlive) return@forEach

                if(showBoundingBox) drawWireFrameBox(entity.boundingBox, Colors.MINECRAFT_RED)
                if(showTracer) drawTracer(entity.boundingBox.center, Colors.MINECRAFT_RED, false)
            }
        }

        on<WorldEvent.Load> {
            entities.clear()
            trapperTimer = -1
            trapperPromptTimer = 0
        }
    }

    private fun isValidEntity(entity: Entity): Boolean =
        when (entity) {
            is ArmorStand -> false
            is Player -> false
            is Arrow -> false
            else -> !entity.isInvisible && entity.isAlive
        }

}