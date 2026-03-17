package com.solidhax.odinextras.events

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents

object EventDispatcher {

    init {
        ClientChunkEvents.CHUNK_LOAD.register { _, chunk -> ClientLoadChunkEvent(chunk).postAndCatch() }
    }

}