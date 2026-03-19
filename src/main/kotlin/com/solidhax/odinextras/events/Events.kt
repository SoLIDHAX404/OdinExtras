package com.solidhax.odinextras.events

import com.odtheking.odin.events.core.CancellableEvent
import com.odtheking.odin.events.core.Event
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.chunk.LevelChunk

abstract class PlayerGuiEvent(val gui: Gui) : CancellableEvent() {
    class DrawSlot(gui: Gui, val guiGraphics: GuiGraphics, val x: Int, val y: Int, val item: ItemStack) : PlayerGuiEvent(gui)
}

class RenderTooltipEvent(val lore: MutableList<ClientTooltipComponent>, val x: Int, val y: Int) : CancellableEvent() {}

class SetEntityMetadataEvent(val entity: Entity, val packet: ClientboundSetEntityDataPacket) : CancellableEvent() {}

class ClientLoadChunkEvent(val chunk: LevelChunk) : Event {}