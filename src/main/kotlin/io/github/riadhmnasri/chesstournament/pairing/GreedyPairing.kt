package io.github.riadhmnasri.chesstournament.pairing

/**
 * Pairs [ranked] entrants (players or teams, best first) greedily from the
 * top down: each one is matched with the highest-ranked remaining entrant
 * it has not already faced, according to [havePlayed]. If every remaining
 * entrant is a repeat, the next available one is taken anyway rather than
 * leaving the round unpairable.
 *
 * Returns the pairs in pairing order, the higher-ranked entrant first in
 * each pair. [ranked] must have an even size (take the bye out first).
 */
internal fun <T> pairGreedily(
    ranked: List<T>,
    havePlayed: (T, T) -> Boolean,
): List<Pair<T, T>> {
    val remaining = ranked.toMutableList()
    val pairs = mutableListOf<Pair<T, T>>()
    while (remaining.isNotEmpty()) {
        val top = remaining.removeAt(0)
        val opponentIndex =
            remaining
                .indexOfFirst { candidate -> !havePlayed(top, candidate) }
                .takeIf { it >= 0 } ?: 0
        pairs.add(top to remaining.removeAt(opponentIndex))
    }
    return pairs
}

/**
 * Picks the bye recipient among [ranked] entrants (best first) when their
 * number is odd: the lowest-ranked one for whom [hadBye] is false, or the
 * lowest-ranked one overall if everyone already had a bye. Returns `null`
 * when [ranked] has an even size.
 */
internal fun <T> selectBye(
    ranked: List<T>,
    hadBye: (T) -> Boolean,
): T? {
    if (ranked.size % 2 == 0) return null
    return ranked.filterNot(hadBye).ifEmpty { ranked }.last()
}
