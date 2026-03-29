package com.solidhax.odinextras.features.impl.mining.worldscanner

import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.skyblock.Island
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.solidhax.odinextras.events.ClientLoadChunkEvent
import com.solidhax.odinextras.utils.drawWaypoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.LevelChunk

/*
 * Original source:
 * https://github.com/skies-starred/Nebulune/tree/master
 *
 * This file is based on third-party code and has not been written by me.
 */

object WorldScanner : Module(
    name = "Crystal Hollows Scanner",
    description = "A world scanner for a specific structures in the crystal hollows.",
    category = Category.custom("Mining"),
) {
    private val grottos = mutableListOf<Triple<Pair<Int, Int>, BlockPos, Int>>()
    private val structures = mutableListOf<Pair<Structure, Triple<Int, Int, Int>>>()
    private val scannedChunks = mutableListOf<Pair<Int, Int>>()
    private val grottoChunksMap = mutableMapOf<Pair<Int, Int>, Triple<Pair<Int, Int>, BlockPos, Int>>()

    private val scope = CoroutineScope(Dispatchers.Default.limitedParallelism(1))

    init {
        on<ClientLoadChunkEvent> {
            if(!enabled) return@on
            if(LocationUtils.currentArea != Island.CrystalHollows) return@on

            if(Pair(chunk.pos.x, chunk.pos.z) !in scannedChunks) scope.launch { scanChunk(chunk) }
            else scannedChunks.add(Pair(chunk.pos.x, chunk.pos.z))
        }

        on<WorldEvent.Load> {
            grottos.clear()
            structures.clear()
            scannedChunks.clear()
            grottoChunksMap.clear()
        }

        on<RenderEvent.Extract> {
            if(!enabled) return@on
            if(LocationUtils.currentArea != Island.CrystalHollows) return@on

            for(grotto in grottos) {
                val blockPos = grotto.second
                val amountOfBlocks = grotto.third

                drawWaypoint("Fairy Grotto ($amountOfBlocks Blocks)", blockPos, Colors.WHITE)
            }

            for (structure in structures) {
                val pos = structure.second
                val blockPos = BlockPos(pos.first, pos.second, pos.third)

                drawWaypoint(structure.first.displayName, blockPos, Colors.WHITE)
            }
        }
    }

    private fun scanStructure(chunk: LevelChunk, structure: Structure, x: Int, y: Int, z: Int): Boolean {
        if (structure == Structure.FAIRY_GROTTO) return false

        val worldX = chunk.pos.x * 16 + x
        val worldZ = chunk.pos.z * 16 + z
        val worldPos = BlockPos(worldX, y, worldZ)

        if (structure == Structure.WORM_FISHING && (x < 513 || y < 80 || z < 513)) return false
        if (!structure.quarter.testPredicate(worldPos)) return false

        val blockPos = BlockPos.MutableBlockPos()
        for (structureY in structure.blocks.indices) {
            blockPos.set(x, y + structureY, z)
            val (block, enumProperty, expectedValue) = structure.blocks[structureY]
            if (block == null) continue

            val worldState = chunk.getBlockState(blockPos)
            if (!worldState.`is`(block)) return false

            if (enumProperty != null && expectedValue != null) {
                if (
                    !worldState.hasProperty(enumProperty) ||
                    worldState.getValue(enumProperty) != expectedValue
                ) {
                    return false
                }
            }
        }

        return true
    }


    private fun getAllNearbyGrottoChunks(x: Int, z: Int): MutableList<Triple<Pair<Int, Int>, BlockPos, Int>> {
        val result = mutableListOf<Triple<Pair<Int, Int>, BlockPos, Int>>()
        val visited = mutableSetOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()

        queue.add(x to z)

        while (queue.isNotEmpty()) {
            val (cx, cz) = queue.removeFirst()
            val key = cx to cz

            if (!visited.add(key)) continue

            val current = grottoChunksMap[key] ?: continue
            result.add(current)

            for (dx in -1..1) {
                for (dz in -1..1) {
                    if (dx == 0 && dz == 0) continue
                    queue.add(cx + dx to cz + dz)
                }
            }
        }

        return result
    }

    private fun scanChunk(chunk: LevelChunk) {
        val structuresToScan = mutableListOf<Structure>()

        for(structure in Structure.entries) {
            if(!structures.any{ it.first == structure }) {
                structuresToScan.add(structure)
            }
        }

        val worldPos = BlockPos.MutableBlockPos()
        val chunkPos = BlockPos.MutableBlockPos()
        val chunkJasperBlocks = mutableSetOf<BlockPos>()
        val chunkX = chunk.pos.x
        val chunkZ = chunk.pos.z

        val foundStructure = structures.mapTo(mutableSetOf()) { it.first }

        for(x in 0..15) {
            for(z in 0..15) {
                for(y in 0..169) {
                    val worldX = chunkX * 16 + x
                    val worldZ = chunkZ * 16 + z

                    worldPos.set(worldX, y, worldZ)


                    for (structureToScan in structuresToScan) {
                        if (structureToScan in foundStructure) continue
                        if (!scanStructure(chunk, structureToScan, x, y, z)) continue

                        foundStructure.add(structureToScan)

                        modMessage("${structureToScan.displayName} found at x: $worldX, y: $y, z: $z")

                        structures.add(
                            structureToScan to Triple(
                                worldX + structureToScan.offsetX,
                                y + structureToScan.offsetY,
                                worldZ + structureToScan.offsetZ
                            )
                        )
                    }

                    chunkPos.set(x, y, z)
                    val state = chunk.getBlockState(chunkPos)

                    if (state.`is`(Blocks.MAGENTA_STAINED_GLASS_PANE) || state.`is`(Blocks.MAGENTA_STAINED_GLASS)) {
                        worldPos.set(chunk.pos.x * 16 + x, y, chunk.pos.z * 16 + z)
                        if (!CrystalHollowsQuarter.NUCLEUS.testPredicate(worldPos)) chunkJasperBlocks.add(worldPos.immutable())
                    }
                }
            }
        }


        if (chunkJasperBlocks.isEmpty()) return
        val size = chunkJasperBlocks.size

        val center = BlockPos(
            (chunkJasperBlocks.sumOf { it.x }.toDouble() / size).toInt(),
            (chunkJasperBlocks.sumOf { it.y }.toDouble() / size).toInt(),
            (chunkJasperBlocks.sumOf { it.z }.toDouble() / size).toInt()
        )

        if (CrystalHollowsQuarter.NUCLEUS.testPredicate(center)) return

        grottoChunksMap[Pair(chunkX, chunkZ)] = Triple(Pair(chunkX, chunkZ), center, chunkJasperBlocks.size)

        val cluster = getAllNearbyGrottoChunks(chunkX, chunkZ)
        if (cluster.isEmpty()) return

        val merged = BlockPos(
            cluster.sumOf { it.second.x } / cluster.size,
            cluster.sumOf { it.second.y } / cluster.size,
            cluster.sumOf { it.second.z } / cluster.size
        )

        val numGrottos = grottos.size

        grottos.removeIf { grotto ->
            cluster.any { it.first.first == grotto.first.first && it.first.second == grotto.first.second }
        }

        grottos.add(Triple(Pair(chunkX, chunkZ), merged, cluster.sumOf { it.third }))

        if(numGrottos != grottos.size)
            modMessage("Fairy Grotto found at x: ${merged.x}, y: ${merged.y}, z: ${merged.z}")
    }
}