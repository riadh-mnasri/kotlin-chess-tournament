package io.github.riadhmnasri.chesstournament.standings

import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round

private const val WIN_POINTS = 1.0

/**
 * The number of decisive games each player won across [rounds]. Draws
 * and losses don't count, and neither does a bye: it isn't a decisive
 * game against a real opponent, same convention as [buchholz] and
 * [sonnebornBerger] excluding byes as "not real opponents".
 *
 * Unlike [buchholz], this is not currently wired into [computeStandings]
 * or [Standing] (a separate decision about where it should slot into the
 * tie-break order); call it directly if you want to use it in your own
 * ranking logic.
 */
fun numberOfWins(
    players: List<Player>,
    rounds: List<Round>,
): Map<Player, Int> {
    val games = rounds.flatMap { it.games }
    return players.associateWith { player -> games.count { game -> game.pointsFor(player) == WIN_POINTS } }
}
