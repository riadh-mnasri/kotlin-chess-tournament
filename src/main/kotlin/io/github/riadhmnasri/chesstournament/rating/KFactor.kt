package io.github.riadhmnasri.chesstournament.rating

private const val JUNIOR_AGE_CUTOFF = 18
private const val JUNIOR_RATING_CUTOFF = 2300
private const val TOP_PLAYER_RATING_CUTOFF = 2400
private const val NEW_PLAYER_GAMES_CUTOFF = 30

private const val JUNIOR_K_FACTOR = 40
private const val STANDARD_K_FACTOR = 20
private const val TOP_PLAYER_K_FACTOR = 10

/**
 * A simplified, FIDE-inspired K-factor rule.
 *
 * This is a pragmatic subset of the official FIDE K-factor table, not a
 * full implementation of it: it does not vary by federation, and it does
 * not keep K at 10 for life once a player's rating has ever reached
 * [TOP_PLAYER_RATING_CUTOFF] (it only looks at the player's current
 * rating). It exists to give sensible defaults for tournaments run with
 * this library; callers who need the exact FIDE table are expected to
 * compute their own K-factor and call [newRating] directly.
 *
 * The rule applied here is:
 * - 40 for a player new to the rating list, i.e. who has played fewer
 *   than [NEW_PLAYER_GAMES_CUTOFF] rated games in total (when
 *   [ratedGamesPlayed] is known), regardless of rating or age; this takes
 *   priority over every other rule below, since a brand new player can't
 *   meaningfully have an established lifetime top-rating history yet
 * - 10 for anyone currently rated [TOP_PLAYER_RATING_CUTOFF] or above, or
 *   whose rating has ever reached it before ([hasEverReachedTopRating])
 * - 40 for a junior (under [JUNIOR_AGE_CUTOFF]) rated below [JUNIOR_RATING_CUTOFF]
 * - 20 otherwise
 *
 * [age] and [ratedGamesPlayed] can both be omitted when unknown; the
 * player is then treated as an established adult for the purpose of this
 * rule. [hasEverReachedTopRating] is the caller's responsibility to track
 * and pass in: this remains a per-call snapshot function with no
 * persistent player history of its own.
 */
fun kFactorFor(
    rating: Int,
    age: Int? = null,
    ratedGamesPlayed: Int? = null,
    hasEverReachedTopRating: Boolean = false,
): Int =
    when {
        ratedGamesPlayed != null && ratedGamesPlayed < NEW_PLAYER_GAMES_CUTOFF -> JUNIOR_K_FACTOR
        rating >= TOP_PLAYER_RATING_CUTOFF || hasEverReachedTopRating -> TOP_PLAYER_K_FACTOR
        age != null && age < JUNIOR_AGE_CUTOFF && rating < JUNIOR_RATING_CUTOFF -> JUNIOR_K_FACTOR
        else -> STANDARD_K_FACTOR
    }
