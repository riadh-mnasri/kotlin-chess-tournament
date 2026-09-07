package io.github.riadhmnasri.chesstournament.pairing

import io.github.riadhmnasri.chesstournament.model.Pairing
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round
import io.github.riadhmnasri.chesstournament.standings.computeStandings

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

/**
 * Pairs players for a round after the first, given the games already
 * played in [previousRounds].
 *
 * Players are ranked by their current standing (score, then the
 * tie-break criteria from [io.github.riadhmnasri.chesstournament.standings.computeStandings]).
 * The bye, if needed, goes to the lowest-ranked player who has not had
 * one yet (see [selectByePlayer]). The remaining players are paired
 * greedily from the top of the ranking down: each player is matched
 * with the highest-ranked remaining player they have not already faced.
 * Because players with the same score sit next to each other in the
 * ranking, this naturally pairs players within the same score group
 * first, and only reaches into a neighbouring score group when a repeat
 * pairing would otherwise be unavoidable (a "floater", in Swiss
 * pairing terms). Colors are then assigned by [allocateColors].
 *
 * If a player has, in the worst case, already faced every other
 * remaining player, they are re-paired with the next available player
 * rather than leaving the round unpairable; this is a known limitation
 * for very small or very long tournaments, see the project README.
 *
 * [virtualPointsByPlayer] optionally adds a bonus to a player's score for
 * *pairing purposes only* (real standings and scores are unaffected): a
 * common technique in large open tournaments, known as accelerated
 * pairings, to separate contenders from the rest of the field faster. It
 * only changes which players are considered tied when ranking for this
 * round; all other tie-break criteria still come from the real
 * [computeStandings] order, since re-sorting a fully tie-broken list by
 * an additional criterion is a stable operation. Leave it empty (the
 * default) for no acceleration. See [acceleratedVirtualPoints] for a
 * ready-made schedule, or compute your own.
 */
fun pairNextRound(
    players: List<Player>,
    previousRounds: List<Round>,
    virtualPointsByPlayer: Map<Player, Double> = emptyMap(),
): RoundPairings {
    val rankedPlayers =
        computeStandings(players, previousRounds)
            .sortedByDescending { standing -> standing.score + (virtualPointsByPlayer[standing.player] ?: 0.0) }
            .map { it.player }
    val byePlayer = selectByePlayer(rankedPlayers, previousRounds)
    val remainingPlayers = rankedPlayers.filterNot { it == byePlayer }.toMutableList()

    val pairings = mutableListOf<Pairing>()
    while (remainingPlayers.isNotEmpty()) {
        val topPlayer = remainingPlayers.removeAt(0)
        val opponentIndex =
            remainingPlayers
                .indexOfFirst { candidate -> !havePlayed(topPlayer, candidate, previousRounds) }
                .takeIf { it >= 0 } ?: 0
        val opponent = remainingPlayers.removeAt(opponentIndex)

        val (white, black) = allocateColors(topPlayer, opponent, previousRounds)
        pairings.add(Pairing(white = white, black = black))
    }

    return RoundPairings(pairings = pairings, byePlayer = byePlayer)
}

private fun havePlayed(
    playerA: Player,
    playerB: Player,
    rounds: List<Round>,
): Boolean =
    rounds
        .flatMap { it.games }
        .any { game ->
            (game.white == playerA && game.black == playerB) || (game.white == playerB && game.black == playerA)
        }
