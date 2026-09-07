package io.github.riadhmnasri.chesstournament.standings

import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round

private const val BYE_POINTS = 1.0

/**
 * Computes the ranked standings for [players] after [rounds] have been played.
 *
 * Players are sorted by score, then by [buchholz], then by [sonnebornBerger],
 * then by [averageRatingOfOpponents], then by [directEncounter] (only
 * meaningful, and only computed, within a group of players still fully
 * tied after the first four criteria). Ties that survive all five criteria
 * are broken alphabetically by player name so the result is always
 * deterministic; the official FIDE tie-break systems define further
 * criteria that this library does not implement yet.
 */
fun computeStandings(
    players: List<Player>,
    rounds: List<Round>,
): List<Standing> {
    val scoreByPlayer = players.associateWith { player -> totalScore(player, rounds) }
    val buchholzByPlayer = buchholz(players, rounds, scoreByPlayer)
    val sonnebornBergerByPlayer = sonnebornBerger(players, rounds, scoreByPlayer)
    val averageRatingOfOpponentsByPlayer = averageRatingOfOpponents(players, rounds)

    val tieBreakKey = { player: Player ->
        listOf(
            scoreByPlayer.getValue(player),
            buchholzByPlayer.getValue(player),
            sonnebornBergerByPlayer.getValue(player),
            averageRatingOfOpponentsByPlayer.getValue(player),
        )
    }
    val directEncounterByPlayer =
        players
            .groupBy(tieBreakKey)
            .values
            .flatMap { group -> directEncounter(group, rounds).entries }
            .associate { it.key to it.value }

    return players
        .map { player ->
            Standing(
                player = player,
                score = scoreByPlayer.getValue(player),
                buchholz = buchholzByPlayer.getValue(player),
                sonnebornBerger = sonnebornBergerByPlayer.getValue(player),
                averageRatingOfOpponents = averageRatingOfOpponentsByPlayer.getValue(player),
                directEncounter = directEncounterByPlayer.getValue(player),
            )
        }.sortedWith(
            compareByDescending<Standing> { it.score }
                .thenByDescending { it.buchholz }
                .thenByDescending { it.sonnebornBerger }
                .thenByDescending { it.averageRatingOfOpponents }
                .thenByDescending { it.directEncounter }
                .thenBy { it.player.name },
        )
}

private fun totalScore(
    player: Player,
    rounds: List<Round>,
): Double {
    val gamePoints = rounds.flatMap { it.games }.sumOf { game -> game.pointsFor(player) }
    val byePoints = rounds.count { round -> round.byePlayer == player } * BYE_POINTS
    return gamePoints + byePoints
}
