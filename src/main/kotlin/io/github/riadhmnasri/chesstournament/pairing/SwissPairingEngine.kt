package io.github.riadhmnasri.chesstournament.pairing

import io.github.riadhmnasri.chesstournament.model.Pairing
import io.github.riadhmnasri.chesstournament.model.Player

/**
 * Pairs players for the first round of a Swiss tournament using the
 * standard top-half-versus-bottom-half method: players are ranked by
 * rating, split into two equal halves, and each player in the top half
 * faces the player in the same position of the bottom half.
 *
 * If there is an odd number of players, the single lowest-rated player
 * receives a bye and the rest are paired as above.
 *
 * This is a pragmatic implementation of the Dutch system's first round,
 * not the full FIDE specification: color allocation here simply
 * alternates from one pairing to the next, rather than following the
 * official initial-color rules based on each player's color preference
 * history.
 */
fun pairFirstRound(players: List<Player>): RoundPairings {
    val rankedPlayers = players.sortedByDescending { it.rating }
    val byePlayer = rankedPlayers.lastOrNull().takeIf { rankedPlayers.size % 2 != 0 }
    val pairablePlayers = if (byePlayer != null) rankedPlayers.dropLast(1) else rankedPlayers

    val halfSize = pairablePlayers.size / 2
    val topHalf = pairablePlayers.take(halfSize)
    val bottomHalf = pairablePlayers.takeLast(halfSize)

    val pairings =
        topHalf.zip(bottomHalf).mapIndexed { index, (topPlayer, bottomPlayer) ->
            if (index % 2 == 0) {
                Pairing(white = topPlayer, black = bottomPlayer)
            } else {
                Pairing(white = bottomPlayer, black = topPlayer)
            }
        }

    return RoundPairings(pairings = pairings, byePlayer = byePlayer)
}
