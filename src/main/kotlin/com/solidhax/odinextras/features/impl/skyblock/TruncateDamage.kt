package com.solidhax.odinextras.features.impl.skyblock

import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.solidhax.odinextras.events.SetEntityMetadataEvent
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.decoration.ArmorStand

object TruncateDamage : Module(
    name = "Truncate Damage",
    description = "Truncate the damage splash number",
) {

    private val damageRegex = Regex("""^(?<prefix>[✧✯]?)(?<number>\d{1,3}(?:,\d{3})*)(?<suffix>[⚔+✧❤♞☄✷ﬗ✯]*)$""")

    private val colorsHypixel = arrayOf(
        ChatFormatting.WHITE,
        ChatFormatting.YELLOW,
        ChatFormatting.GOLD,
        ChatFormatting.RED,
        ChatFormatting.RED,
        ChatFormatting.WHITE
    )

    init {
        on<SetEntityMetadataEvent> {
            if (!enabled || !LocationUtils.isInSkyblock) return@on

            val armorStandEntity = entity as? ArmorStand ?: return@on
            val name = armorStandEntity.customName?.string ?: return@on
            val match = damageRegex.matchEntire(name) ?: return@on

            val prefix = match.groups["prefix"]!!.value
            val number = match.groups["number"]!!.value
            val suffix = match.groups["suffix"]!!.value

            val isCritical = prefix.indexOfAny(charArrayOf('✧', '✯')) != -1
            val shortened = shortenDamageNumber(number)
            val finalText = prefix + shortened + suffix

            armorStandEntity.customName = buildShortenedDamageTag(finalText, isCritical)
        }
    }

    private fun buildShortenedDamageTag(text: String, isCritical: Boolean): Component {
        val root: MutableComponent = Component.empty()
        val colorsSize = colorsHypixel.size

        if(!isCritical) {
            root.append(Component.literal(text).withStyle(ChatFormatting.GRAY))
            return root
        }

        text.forEachIndexed { index, ch ->
            val color = colorsHypixel[index % colorsSize]
            root.append(Component.literal(ch.toString()).withStyle(color))
        }

        return root
    }

    private fun shortenDamageNumber(raw: String): String {
        val clean = raw.replace(",", "")
        val value = clean.toLongOrNull() ?: return raw

        return when {
            value >= 1_000_000_000L -> format(value, 1_000_000_000L, "b")
            value >= 1_000_000L -> format(value, 1_000_000L, "m")
            value >= 1_000L -> format(value, 1_000L, "k")
            else -> value.toString()
        }
    }

    private fun format(value: Long, divisor: Long, suffix: String): String {
        val scaled = value.toDouble() / divisor.toDouble()
        val out = if (scaled % 1.0 == 0.0) scaled.toLong().toString() else String.format("%.1f", scaled)
        return out + suffix
    }
}