package com.solidhax.odinextras.api

import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import java.util.regex.Pattern

object MiningAPI {
    var commissions: List<Commission> = emptyList()

    private val commissionRegex = Pattern.compile("^\\s*(?<name>[\\w\\s]+?):\\s*(?<progress>[\\d.]+%|\\w+)$")

    init {
        on<TickEvent.Server> {
            val commissionWidget = TabListAPI.getWidget(TabListAPI.TabWidget.COMMISSIONS) ?: return@on

            commissions = commissionWidget.data.mapNotNull { line ->
                commissionRegex.matcher(line).takeIf { it.find() }?.let {
                    Commission(it.group("name"), parseCommissionProgress(it.group("progress")))
                }
            }
        }
    }

    private fun parseCommissionProgress(progressStr: String): Double = when {
        progressStr == "DONE" -> 100.0
        progressStr.endsWith("%") -> progressStr.dropLast(1).toDouble()
        else -> 0.0
    }

    data class Commission(val name: String, val progress: Double)
}