package io.github.riadhmnasri.chesstournament.model

/**
 * The outcome of a single game, from one specific player's point of view.
 *
 * [points] is the score that player earns for that game under standard
 * chess tournament scoring: a full point for a win, half a point for a
 * draw, nothing for a loss.
 */
enum class GameResult(val points: Double) {
    WIN(1.0),
    DRAW(0.5),
    LOSS(0.0),
}
