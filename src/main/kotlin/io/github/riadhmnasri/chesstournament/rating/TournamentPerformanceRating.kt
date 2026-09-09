package io.github.riadhmnasri.chesstournament.rating

private const val LOWEST_PLAUSIBLE_RATING = 0
private const val HIGHEST_PLAUSIBLE_RATING = 4000
private const val BINARY_SEARCH_ITERATIONS = 40

/**
 * The rating a player would need to have a [expectedScore] against every
 * opponent in [opponentRatings] that sums to exactly [actualScore], found
 * by binary search over a plausible rating range (rather than the
 * traditional "dp lookup table" approximation, since [expectedScore] is
 * already available here for an exact answer).
 *
 * [opponentRatings] should already exclude byes (they are not real
 * opponents), same convention as [buchholz][io.github.riadhmnasri.chesstournament.standings.buchholz].
 *
 * A score at or near the extremes (0 or a perfect score) converges
 * toward [LOWEST_PLAUSIBLE_RATING] or [HIGHEST_PLAUSIBLE_RATING]
 * respectively, since no finite rating produces a 0% or 100% expected
 * score against a real opponent.
 */
fun tournamentPerformanceRating(
    actualScore: Double,
    opponentRatings: List<Int>,
): Int {
    require(opponentRatings.isNotEmpty()) { "Cannot compute a performance rating with no opponents" }

    var low = LOWEST_PLAUSIBLE_RATING.toDouble()
    var high = HIGHEST_PLAUSIBLE_RATING.toDouble()

    repeat(BINARY_SEARCH_ITERATIONS) {
        val mid = (low + high) / 2.0
        val expectedTotal = opponentRatings.sumOf { opponentRating -> expectedScore(mid.toInt(), opponentRating) }
        if (expectedTotal < actualScore) low = mid else high = mid
    }

    return ((low + high) / 2.0).toInt()
}
