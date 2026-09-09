package io.github.riadhmnasri.chesstournament.pairing

import io.github.riadhmnasri.chesstournament.model.Player
import kotlin.math.ceil

private const val GA_GROUP_SIZE_DIVISOR = 4.0
private const val GA_GROUP_SIZE_MULTIPLIER = 2
private const val FULL_VIRTUAL_POINT = 1.0
private const val HALF_VIRTUAL_POINT = 0.5

/**
 * FIDE's official Baku Acceleration algorithm (FIDE Handbook, C.04.7,
 * effective from 1 February 2026), a group-size- and round-count-aware
 * alternative to [acceleratedVirtualPoints]'s simplified, fixed schedule.
 *
 * Players are split into two groups: GA, the top-rated `2 * ceil(N / 4)`
 * players (`N` = [players].size, capped at `N` for very small fields),
 * and GB, everyone else. The number of accelerated rounds is
 * `ceil(totalRounds / 2)`; during the first `ceil(acceleratedRounds / 2)`
 * of those, GA gets a full virtual point ([FULL_VIRTUAL_POINT]); during
 * the remaining accelerated rounds, GA gets half a virtual point
 * ([HALF_VIRTUAL_POINT]). GB never receives virtual points, and no
 * virtual points are given once [roundNumber] exceeds the accelerated
 * rounds. For example, in a 9-round tournament (5 accelerated rounds: 3
 * full, 2 half), this exactly reproduces the schedule given as a worked
 * example in the FIDE Handbook.
 *
 * The result is meant to be passed straight to [pairNextRound]'s
 * `virtualPointsByPlayer` parameter, same as [acceleratedVirtualPoints].
 */
fun bakuAcceleratedVirtualPoints(
    players: List<Player>,
    roundNumber: Int,
    totalRounds: Int,
): Map<Player, Double> {
    require(roundNumber in 1..totalRounds) {
        "roundNumber ($roundNumber) must be between 1 and totalRounds ($totalRounds)"
    }

    val acceleratedRounds = ceil(totalRounds / 2.0).toInt()
    if (roundNumber > acceleratedRounds) return players.associateWith { 0.0 }

    val fullPointRounds = ceil(acceleratedRounds / 2.0).toInt()
    val bonus = if (roundNumber <= fullPointRounds) FULL_VIRTUAL_POINT else HALF_VIRTUAL_POINT

    val uncappedGaSize = GA_GROUP_SIZE_MULTIPLIER * ceil(players.size / GA_GROUP_SIZE_DIVISOR).toInt()
    val gaSize = uncappedGaSize.coerceAtMost(players.size)
    val ga = players.sortedByDescending { it.rating }.take(gaSize).toSet()

    return players.associateWith { player -> if (player in ga) bonus else 0.0 }
}
