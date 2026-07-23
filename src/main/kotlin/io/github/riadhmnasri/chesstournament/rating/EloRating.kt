package io.github.riadhmnasri.chesstournament.rating

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * The Elo formula scales a rating gap of this many points to a ten to one
 * expected outcome between the two players.
 */
private const val ELO_RATING_SCALE = 400.0

/**
 * The probability, expressed as a score between 0.0 and 1.0, that a player
 * rated [playerRating] is expected to score against an opponent rated
 * [opponentRating] under the standard Elo formula.
 *
 * A 400 point rating gap corresponds to roughly a ten to one expected
 * outcome in favor of the stronger player.
 */
fun expectedScore(
    playerRating: Int,
    opponentRating: Int,
): Double {
    val ratingGap = (opponentRating - playerRating) / ELO_RATING_SCALE
    return 1.0 / (1.0 + 10.0.pow(ratingGap))
}

/**
 * The player's rating after a single game, given the score they were
 * [expectedScore] to achieve, the score they actually achieved
 * ([actualScore], see [io.github.riadhmnasri.chesstournament.model.GameResult.points]),
 * and the [kFactor] that controls how much a single result can move the rating.
 */
fun newRating(
    currentRating: Int,
    expectedScore: Double,
    actualScore: Double,
    kFactor: Int,
): Int = (currentRating + kFactor * (actualScore - expectedScore)).roundToInt()
