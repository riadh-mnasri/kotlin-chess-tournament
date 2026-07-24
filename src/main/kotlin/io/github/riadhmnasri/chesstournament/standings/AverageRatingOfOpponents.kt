package io.github.riadhmnasri.chesstournament.standings

import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round

/**
 * The Average Rating of Opponents (ARO) tie-break score for each player:
 * the mean rating of every opponent that player actually faced across the
 * tournament.
 *
 * Byes are excluded, same as for [buchholz] and [sonnebornBerger], since
 * there is no real opponent rating to average in. A player with no games
 * yet has an ARO of 0.
 */
internal fun averageRatingOfOpponents(
    players: List<Player>,
    rounds: List<Round>,
): Map<Player, Double> =
    players.associateWith { player ->
        val opponentRatings =
            rounds
                .flatMap { it.games }
                .mapNotNull { game -> game.opponentOf(player) }
                .map { opponent -> opponent.rating }
        if (opponentRatings.isEmpty()) 0.0 else opponentRatings.average()
    }
