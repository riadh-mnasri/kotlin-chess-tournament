package io.github.riadhmnasri.chesstournament.pairing

import io.github.riadhmnasri.chesstournament.model.Player

private const val ROUND_1_VIRTUAL_POINTS = 1.0
private const val ROUND_2_VIRTUAL_POINTS = 0.5
private const val LAST_ACCELERATED_ROUND = 2

/**
 * A simple, common accelerated pairings scheme: the top half of [players]
 * by rating gets a virtual bonus of [ROUND_1_VIRTUAL_POINTS] in round 1
 * and [ROUND_2_VIRTUAL_POINTS] in round 2, then no bonus from round 3
 * onward. The bottom half never gets a bonus.
 *
 * This is one specific simplified scheme among several real-world
 * variants (for example, FIDE's Baku acceleration uses a different,
 * group-size-dependent schedule); it exists as a ready-made convenience
 * for callers who just want *a* reasonable acceleration. Callers who need
 * a different schedule can compute their own `Map<Player, Double>` and
 * pass it to [pairNextRound]'s `virtualPointsByPlayer` parameter directly.
 *
 * The result is meant to be passed straight to [pairNextRound]; it has no
 * effect on real standings or scores.
 */
fun acceleratedVirtualPoints(
    players: List<Player>,
    roundNumber: Int,
): Map<Player, Double> {
    val bonus =
        when (roundNumber) {
            1 -> ROUND_1_VIRTUAL_POINTS
            LAST_ACCELERATED_ROUND -> ROUND_2_VIRTUAL_POINTS
            else -> 0.0
        }

    if (bonus == 0.0) return players.associateWith { 0.0 }

    val topHalfSize = players.size / 2
    val topHalf = players.sortedByDescending { it.rating }.take(topHalfSize).toSet()

    return players.associateWith { player -> if (player in topHalf) bonus else 0.0 }
}
