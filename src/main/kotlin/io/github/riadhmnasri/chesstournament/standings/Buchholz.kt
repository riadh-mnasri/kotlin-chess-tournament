package io.github.riadhmnasri.chesstournament.standings

import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round

/**
 * The Buchholz tie-break score for each player: the sum of the final
 * scores of every opponent that player actually faced across the
 * tournament.
 *
 * Byes are not real opponents and are excluded, since there is no
 * opponent score to attribute to them. [scoreByPlayer] must already
 * contain the final score of every player passed in [players].
 */
internal fun buchholz(
    players: List<Player>,
    rounds: List<Round>,
    scoreByPlayer: Map<Player, Double>,
): Map<Player, Double> =
    players.associateWith { player ->
        rounds
            .flatMap { it.games }
            .mapNotNull { game -> game.opponentOf(player) }
            .sumOf { opponent -> scoreByPlayer.getValue(opponent) }
    }
