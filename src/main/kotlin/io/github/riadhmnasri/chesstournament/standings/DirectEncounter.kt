package io.github.riadhmnasri.chesstournament.standings

import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round

/**
 * The direct encounter tie-break score for each player in [group]: their
 * mini-score across games played only against other members of [group].
 *
 * This only has a meaningful value when [group] forms a complete mini
 * round-robin, i.e. every pair of players in it played each other exactly
 * once. When that isn't the case (a pair never played, played more than
 * once, or [group] has fewer than two players), every player in [group]
 * gets a neutral 0.0, so this criterion has no effect and standings fall
 * through to the next one.
 *
 * Deliberately not implemented as a pairwise comparator: a mini-score is a
 * single numeric value per player, so sorting by it descending is always
 * transitive, even when the underlying results contain a cycle (A beat B,
 * B beat C, C beat A), which a naive head-to-head comparator cannot
 * handle safely.
 */
internal fun directEncounter(
    group: List<Player>,
    rounds: List<Round>,
): Map<Player, Double> {
    val groupSet = group.toSet()
    val gamesWithinGroup =
        rounds
            .flatMap { it.games }
            .filter { game -> game.white in groupSet && game.black in groupSet }

    if (!isCompleteMiniRoundRobin(group, gamesWithinGroup)) {
        return group.associateWith { 0.0 }
    }

    return group.associateWith { player -> gamesWithinGroup.sumOf { game -> game.pointsFor(player) } }
}

private fun isCompleteMiniRoundRobin(
    group: List<Player>,
    gamesWithinGroup: List<Game>,
): Boolean {
    val expectedPairs = group.size * (group.size - 1) / 2
    val playedPairs = gamesWithinGroup.map { game -> setOf(game.white, game.black) }.toSet()
    return expectedPairs > 0 && gamesWithinGroup.size == expectedPairs && playedPairs.size == expectedPairs
}
