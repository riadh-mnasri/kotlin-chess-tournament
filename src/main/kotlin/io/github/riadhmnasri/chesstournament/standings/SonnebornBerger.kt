package io.github.riadhmnasri.chesstournament.standings

import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round

private const val WIN_POINTS = 1.0
private const val DRAW_POINTS = 0.5

/**
 * The Sonneborn-Berger tie-break score for each player: for every real
 * opponent a player faced, the opponent's final score counts in full for
 * a win, halved for a draw, and not at all for a loss.
 *
 * Byes are excluded, same as for [buchholz]. [scoreByPlayer] must already
 * contain the final score of every player passed in [players].
 */
internal fun sonnebornBerger(
    players: List<Player>,
    rounds: List<Round>,
    scoreByPlayer: Map<Player, Double>,
): Map<Player, Double> =
    players.associateWith { player ->
        rounds
            .flatMap { it.games }
            .sumOf { game -> game.contributionTo(player, scoreByPlayer) }
    }

private fun Game.contributionTo(
    player: Player,
    scoreByPlayer: Map<Player, Double>,
): Double {
    val opponent = opponentOf(player) ?: return 0.0
    val opponentScore = scoreByPlayer.getValue(opponent)
    return when (pointsFor(player)) {
        WIN_POINTS -> opponentScore
        DRAW_POINTS -> opponentScore * DRAW_POINTS
        else -> 0.0
    }
}
