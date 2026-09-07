package io.github.riadhmnasri.chesstournament.standings

import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round

private const val MINIMUM_OPPONENTS_TO_CUT = 2

/**
 * The Buchholz Cut-1 (a.k.a. median Buchholz) tie-break score for each
 * player: the same sum as [buchholz], but with the single lowest-scoring
 * real opponent excluded, to reduce the effect of one weak pairing on the
 * tie-break.
 *
 * A player with fewer than [MINIMUM_OPPONENTS_TO_CUT] real opponents has
 * nothing meaningful to cut, so their Buchholz Cut-1 equals their plain
 * Buchholz. When several opponents tie for the lowest score, only one
 * instance is excluded. Byes are excluded from the opponent list, same as
 * for [buchholz]. [scoreByPlayer] must already contain the final score of
 * every player passed in [players].
 *
 * Unlike [buchholz], this is not currently wired into [computeStandings]
 * or [Standing] (a separate decision about where it should slot into the
 * tie-break order); call it directly if you want to use it in your own
 * ranking logic.
 */
fun buchholzCut1(
    players: List<Player>,
    rounds: List<Round>,
    scoreByPlayer: Map<Player, Double>,
): Map<Player, Double> {
    val games = rounds.flatMap { it.games }

    return players.associateWith { player ->
        val opponentScores = games.mapNotNull { game -> game.opponentOf(player) }.map { scoreByPlayer.getValue(it) }
        if (opponentScores.size < MINIMUM_OPPONENTS_TO_CUT) {
            opponentScores.sum()
        } else {
            opponentScores.sum() - opponentScores.min()
        }
    }
}
