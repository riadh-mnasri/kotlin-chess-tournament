package io.github.riadhmnasri.chesstournament.pairing

import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round

/**
 * Picks the bye recipient for a round with an odd number of players.
 *
 * [rankedPlayers] must already be sorted from best to worst. The bye goes
 * to the lowest-ranked player who has not already received one earlier
 * in the tournament, so the bye rotates instead of always falling on the
 * same player. In the rare case where every player has already had a
 * bye, the lowest-ranked player receives a second one rather than
 * leaving the round unpairable.
 *
 * Returns `null` when [rankedPlayers] has an even size.
 */
internal fun selectByePlayer(
    rankedPlayers: List<Player>,
    previousRounds: List<Round>,
): Player? {
    if (rankedPlayers.size % 2 == 0) return null

    val playersWithoutAPriorBye =
        rankedPlayers.filter { player ->
            previousRounds.none { round -> round.byePlayer == player }
        }

    return playersWithoutAPriorBye.ifEmpty { rankedPlayers }.last()
}
