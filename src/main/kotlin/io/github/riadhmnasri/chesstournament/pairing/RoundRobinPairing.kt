package io.github.riadhmnasri.chesstournament.pairing

import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.GameOutcome
import io.github.riadhmnasri.chesstournament.model.Pairing
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round

/**
 * Generates the full round schedule for a round-robin tournament, all at
 * once: unlike [pairFirstRound]/[pairNextRound], round-robin pairings
 * don't depend on results, so there is nothing to compute round by round.
 *
 * Uses the standard "circle method": one player is fixed, the rest
 * rotate one position each round, so every player faces every other
 * exactly once. With an odd number of players, a virtual bye slot is
 * added, so there are N rounds instead of N-1 and every player sits out
 * exactly one of them, rotating the same way [selectByePlayer] does for
 * the Swiss functions, just computed for the whole schedule at once.
 *
 * [doubleRoundRobin] appends a second cycle, identical in pairing
 * structure to the first but with colors swapped, the conventional way
 * to run a double round-robin fairly. Colors within a single cycle are
 * allocated via [allocateColors], applied in schedule order.
 */
fun pairRoundRobin(
    players: List<Player>,
    doubleRoundRobin: Boolean = false,
): List<RoundPairings> {
    require(players.size >= 2) { "Round-robin pairing requires at least 2 players, got ${players.size}" }

    val singleCycle = singleRoundRobinCycle(players)
    return if (doubleRoundRobin) singleCycle + singleCycle.map { it.withSwappedColors() } else singleCycle
}

private fun singleRoundRobinCycle(players: List<Player>): List<RoundPairings> {
    val slots: List<Player?> = if (players.size % 2 != 0) players + listOf(null) else players
    val slotCount = slots.size
    val roundCount = slotCount - 1

    val schedule = mutableListOf<RoundPairings>()
    // Games with a placeholder outcome, used only so allocateColors can see
    // who already had which color in earlier rounds of this schedule; the
    // outcome itself is meaningless and never reaches the returned schedule.
    val colorHistory = mutableListOf<Round>()
    var current = slots

    repeat(roundCount) { roundIndex ->
        val pairings = mutableListOf<Pairing>()
        var byePlayer: Player? = null

        for (i in 0 until slotCount / 2) {
            val playerA = current[i]
            val playerB = current[slotCount - 1 - i]
            when {
                playerA == null -> byePlayer = playerB
                playerB == null -> byePlayer = playerA
                else -> {
                    val (white, black) = allocateColors(playerA, playerB, colorHistory)
                    pairings.add(Pairing(white = white, black = black))
                }
            }
        }

        schedule.add(RoundPairings(pairings = pairings, byePlayer = byePlayer))
        colorHistory.add(
            Round(
                number = roundIndex + 1,
                games = pairings.map { Game(it.white, it.black, GameOutcome.DRAW) },
                byePlayer = byePlayer,
            ),
        )
        current = rotate(current)
    }

    return schedule
}

/** Keeps the first slot fixed and rotates the rest by one position, the core step of the circle method. */
private fun rotate(slots: List<Player?>): List<Player?> {
    val rest = slots.drop(1)
    return listOf(slots.first()) + listOf(rest.last()) + rest.dropLast(1)
}

private fun RoundPairings.withSwappedColors(): RoundPairings =
    copy(pairings = pairings.map { Pairing(white = it.black, black = it.white) })
