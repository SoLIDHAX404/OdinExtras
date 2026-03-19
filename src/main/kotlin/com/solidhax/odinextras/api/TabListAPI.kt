package com.solidhax.odinextras.api

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.world.level.GameType
import java.util.regex.Pattern

// Heavily inspired by SkyblockAPI
object TabListAPI {

    val tabListComparator = compareBy<PlayerInfo>(
        { if (it.gameMode == GameType.SPECTATOR) 1 else 0 },
        { it.team?.name ?: "" },
        { it.profile.name }
    )

    data class WidgetContent(
        val header: String,
        val data: List<String> = emptyList()
    )

    enum class TabWidget(val regex: Pattern) {
        // General
        AREA(Pattern.compile("(?:Area|Dungeon): (?<area>.*)")),
        PROFILE(Pattern.compile("Profile: (?<profile>.*)")),
        PET(Pattern.compile("Pet:")),
        DAILY_QUESTS(Pattern.compile("Daily Quests:")),
        SKILLS(Pattern.compile("Skills:(?: (?<avg>[\\d.]+)| (?<skill>.+) (?<level>\\S+): (?<progress>[\\d,.kMB%]+))?")),
        ELECTION(Pattern.compile("Election: (?<election>.*)")),
        BESTIARY(Pattern.compile("Bestiary:")),
        COLLECTION(Pattern.compile("Collection:")),
        STATS(Pattern.compile("Stats:")),
        EVENT(Pattern.compile("Event: (?<event>.*)")),
        EVENT_TRACKERS(Pattern.compile("Event Trackers:")),
        ACTIVE_EFFECTS(Pattern.compile("Active Effects:(?: \\((?<amount>\\d+)\\))?")),
        TIMERS(Pattern.compile("Timers:")),
        FIRE_SALE(Pattern.compile("Fire Sales: \\((?<amount>[\\d,.]+)\\)")),
        MINIONS(Pattern.compile("Minions: (?<amount>.*)")),
        PITY(Pattern.compile("Pity:")),
        SLAYER(Pattern.compile("Slayer:")),

        // Mining
        FORGES(Pattern.compile("Forges:(?: \\((?<active>[\\d,.]+)/(?<max>[\\d,.]+)\\))?")),
        COMMISSIONS(Pattern.compile("Commissions:")),
        POWDERS(Pattern.compile("Powders:")),
        CRYSTALS(Pattern.compile("Crystals:")),
        MINING_EVENT(Pattern.compile("Mining Event: (?<event>.*)")),
        FROZEN_CORPSES(Pattern.compile("Frozen Corpses:")),
        PICKAXE_ABILITY(Pattern.compile("Pickaxe Ability:")),
        WORMS(Pattern.compile("Worms:")),

        // Foraging
        AGATHA_CONTEST(Pattern.compile("Agatha's Contest:")),
        STARBORN_TEMPLE(Pattern.compile("Starborn Temple:")),
        FOREST_WHISPERS(Pattern.compile("Forest Whispers: (?<amount>[\\dkmbKMB,.]+)")),
        MOONGLADE_BEACON(Pattern.compile("Moonglade Beacon: (?<amount>[\\d,.]+) Stacks?")),
        SHARD_TRAPS(Pattern.compile("Shard Traps")),

        // Garden + Farming
        COMPOSTER(Pattern.compile("Composter:")),
        JACOBS_CONTEST(Pattern.compile("Jacob's Contest:(?: (?<time>.*))?")),
        PESTS(Pattern.compile("Pests:(?: (?<amount>\\d+))?")),
        PEST_TRAPS(Pattern.compile("Pest Traps: (?<amount>[\\d,.]+)/(?<max>[\\d,.]+)")),
        VISITORS(Pattern.compile("Visitors: \\((?<amount>\\d+)\\)")),
        TRAPPER(Pattern.compile("Trapper:")),
        CROP_MILESTONES(Pattern.compile("Crop Milestones:")),

        // Crimson Isle
        REPUTATION(Pattern.compile("(?:Mage|Barbarian) Reputation:")),
        TROPHY_FISH(Pattern.compile("Trophy Fish:")),
        FACTION_QUESTS(Pattern.compile("Faction Quests:")),

        // End
        DRAGON(Pattern.compile("Dragon: \\((?<type>.+)\\)")),

        // Dungeons + Dungeon Hub
        DOWNED(Pattern.compile("Downed: (?<status>.*)")),
        TEAM_DEATHS(Pattern.compile("Team Deaths: (?<amount>\\d+)")),
        DISCOVERIES(Pattern.compile("Discoveries: (?<amount>\\d+)")),
        PUZZLES(Pattern.compile("Puzzles: \\((?<amount>\\d+)\\)")),
        RNG_METER(Pattern.compile("RNG Meter")),
        PARTY(Pattern.compile("Party: (?<party>.*)")),
        DUNGEONS(Pattern.compile("Dungeons:")),
        ESSENCE(Pattern.compile("Essence:")),

        // Rift
        GOOD_TO_KNOW(Pattern.compile("Good to know:")),
        SHEN(Pattern.compile("Shen: \\((?<duration>[\\ddmsh,]+)\\)")),
        ADVERTISEMENT(Pattern.compile("Advertisement:")),
        ;

        fun matches(text: String): Boolean = regex.matcher(text).matches()
    }

    private val infoHeaderRegex = Pattern.compile("(?:Info|Account Info|Player Stats|Dungeon Stats)$")
    private val widgets = mutableMapOf<TabWidget, WidgetContent>()

    init {
        on<TickEvent.Server> {
            parseTabList()
        }
    }

    private fun parseTabList() {
        val players = mc.connection?.listedOnlinePlayers?.sortedWith(tabListComparator) ?: return

        var tabLines = players.mapNotNull { it.tabListDisplayName?.string }.filter { it.isNotBlank() }
        tabLines = tabLines.filter { !infoHeaderRegex.matcher(it).find() }

        val widgetLines = mutableMapOf<TabWidget, WidgetContent>()
        var currentWidget: TabWidget? = null
        var currentDataLines = mutableListOf<String>()

        for (line in tabLines) {
            val widget = TabWidget.entries.find { it.matches(line) }

            if (widget != null) {
                if (currentWidget != null) {
                    widgetLines[currentWidget] = WidgetContent(
                        header = tabLines[tabLines.indexOf(line) - currentDataLines.size - 1],
                        data = currentDataLines.toList()
                    )
                }
                currentWidget = widget
                currentDataLines = mutableListOf()
                widgetLines.getOrPut(widget) { WidgetContent(header = line) }
            } else if (currentWidget != null && (line.startsWith(" ") || !couldBeUnknownWidgetStart(currentWidget, line))) {
                currentDataLines.add(line)
            }
        }

        if (currentWidget != null && currentDataLines.isNotEmpty()) {
            widgetLines[currentWidget] = WidgetContent(
                header = widgetLines[currentWidget]?.header ?: "",
                data = currentDataLines.toList()
            )
        }

        widgetLines.forEach { (widget, newContent) ->
            val oldContent = widgets[widget]
            if (oldContent != newContent) {
                widgets[widget] = newContent
            }
        }

        widgets.keys.filter { it !in widgetLines }.forEach { widget ->
            widgets.remove(widget)
        }
    }

    private fun couldBeUnknownWidgetStart(currentWidget: TabWidget, string: String): Boolean {
        if (string.startsWith(" ")) return false
        return when (currentWidget) {
            TabWidget.JACOBS_CONTEST -> string != "ACTIVE"
            TabWidget.MINING_EVENT -> !string.startsWith("Ends in: ")
            else -> true
        }
    }

    fun getWidget(widget: TabWidget): WidgetContent? = widgets[widget]
    fun isWidgetActive(widget: TabWidget): Boolean = widget in widgets
    fun getAllActiveWidgets(): Map<TabWidget, WidgetContent> = widgets.toMap()
}