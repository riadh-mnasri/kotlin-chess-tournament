package io.github.riadhmnasri.chesstournament.rating

private const val JUNIOR_AGE_CUTOFF = 18
private const val JUNIOR_RATING_CUTOFF = 2300
private const val TOP_PLAYER_RATING_CUTOFF = 2400

private const val JUNIOR_K_FACTOR = 40
private const val STANDARD_K_FACTOR = 20
private const val TOP_PLAYER_K_FACTOR = 10

/**
 * A simplified, FIDE-inspired K-factor rule.
 *
 * This is a pragmatic subset of the official FIDE K-factor table, not a
 * full implementation of it: it does not track a player's number of rated
 * games, and it does not vary by federation. It exists to give sensible
 * defaults for tournaments run with this library; callers who need the
 * exact FIDE table are expected to compute their own K-factor and call
 * [newRating] directly.
 *
 * The rule applied here is:
 * - 40 for a junior (under [JUNIOR_AGE_CUTOFF]) rated below [JUNIOR_RATING_CUTOFF]
 * - 10 for anyone rated [TOP_PLAYER_RATING_CUTOFF] or above
 * - 20 otherwise
 *
 * [age] can be omitted when it is unknown; the player is then treated as
 * an adult for the purpose of this rule.
 */
fun kFactorFor(
    rating: Int,
    age: Int? = null,
): Int =
    when {
        age != null && age < JUNIOR_AGE_CUTOFF && rating < JUNIOR_RATING_CUTOFF -> JUNIOR_K_FACTOR
        rating >= TOP_PLAYER_RATING_CUTOFF -> TOP_PLAYER_K_FACTOR
        else -> STANDARD_K_FACTOR
    }
