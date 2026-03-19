package com.solidhax.odinextras

import com.odtheking.odin.config.ModuleConfig
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.features.ModuleManager
import com.solidhax.odinextras.api.TabListAPI
import com.solidhax.odinextras.events.EventDispatcher
import com.solidhax.odinextras.features.impl.floor7.DragonWaypoints
import com.solidhax.odinextras.features.impl.mining.CommissionHelper
import com.solidhax.odinextras.features.impl.mining.CorpseFinder
import com.solidhax.odinextras.features.impl.mining.worldscanner.WorldScanner
import com.solidhax.odinextras.features.impl.render.HUD
import com.solidhax.odinextras.features.impl.skyblock.*
import net.fabricmc.api.ClientModInitializer

object OdinExtras : ClientModInitializer {

    override fun onInitializeClient() {
        // Register commands by adding to the array
//        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
//            arrayOf(protectItemCommand).forEach { commodore -> commodore.register(dispatcher) }
//        }

        listOf(this, EventDispatcher, TabListAPI).forEach { EventBus.subscribe(it) }

        ModuleManager.registerModules(ModuleConfig("OdinExtras.json"), RarityDisplay, ItemLore, HUD, DragonWaypoints,
            CorpseFinder, Trapper, TruncateDamage, AuctionHouseHelper, WorldScanner, CommissionHelper, FishingHelper, PreventPlacingSkyblockItems)
    }
}
