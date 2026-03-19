package com.solidhax.odinextras.features.impl.skyblock

import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.customData
import com.odtheking.odin.utils.skyblock.LocationUtils
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

object PreventPlacingSkyblockItems : Module(
    name = "Stop Placing Skyblock Items",
    description = "Prevents placing skyblock items like the Spirit Sceptre, or fishing net"
) {

    private val itemIds: List<String> = listOf(
        "BASIC_FISHING_NET",
        "MEDIUM_FISHING_NET",
        "TURBO_FISHING_NET",
        "FLOWER_OF_TRUTH",
        "BOUQUET_OF_LIES",
        "FIRE_FREEZE_STAFF",
        "WEIRD_TUBA",
        "WEIRDER_TUBA",
        "BAT_WAND",
        "STARRED_BAT_WAND"
    )

    @JvmStatic
    fun shouldCancelPlacement(item: ItemStack): Boolean {
        if (!enabled || !LocationUtils.isInSkyblock) return false

        val id = item.customData.getString("id").getOrNull()
        return itemIds.contains(id)
    }
}