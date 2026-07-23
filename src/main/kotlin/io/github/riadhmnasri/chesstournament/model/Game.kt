package io.github.riadhmnasri.chesstournament.model

private const val DRAW_POINTS = 0.5

/** A single finished game between two players in a given round. */
data class Game(
    val white: Player,
    val black: Player,
    val outcome: GameOutcome,
) {
    /** The opponent [player] faced in this game, or `null` if [player] did not play it. */
    fun opponentOf(player: Player): Player? =
        when (player) {
            white -> black
            black -> white
            else -> null
        }

    /** The points [player] earned in this game, or 0.0 if [player] did not play it. */
    fun pointsFor(player: Player): Double =
        when (player) {
            white ->
                when (outcome) {
                    GameOutcome.WHITE_WINS -> 1.0
                    GameOutcome.BLACK_WINS -> 0.0
                    GameOutcome.DRAW -> DRAW_POINTS
                }
            black ->
                when (outcome) {
                    GameOutcome.WHITE_WINS -> 0.0
                    GameOutcome.BLACK_WINS -> 1.0
                    GameOutcome.DRAW -> DRAW_POINTS
                }
            else -> 0.0
        }
}
