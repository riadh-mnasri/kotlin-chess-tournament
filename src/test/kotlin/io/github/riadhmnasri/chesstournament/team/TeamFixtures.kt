package io.github.riadhmnasri.chesstournament.team

import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.GameOutcome
import io.github.riadhmnasri.chesstournament.model.Player

/** A team whose roster has one player per given rating, in board order. */
internal fun team(
    id: String,
    vararg ratings: Int,
): Team =
    Team(
        id = id,
        name = "Team $id",
        players =
            ratings.mapIndexed { board, rating ->
                Player(id = "$id-${board + 1}", name = "$id ${board + 1}", rating = rating)
            },
    )

/**
 * Plays [pairing] on full rosters, [firstTeamPoints] giving the first team's
 * result on each board (1.0, 0.5 or 0.0), whatever color it had there.
 */
internal fun play(
    pairing: TeamPairing,
    vararg firstTeamPoints: Double,
): TeamMatch {
    val games =
        pairing.boards().zip(firstTeamPoints.toList()).map { (board, points) ->
            val firstIsWhite = board.white in pairing.first.players
            val outcome =
                when {
                    points == 0.5 -> GameOutcome.DRAW
                    (points == 1.0) == firstIsWhite -> GameOutcome.WHITE_WINS
                    else -> GameOutcome.BLACK_WINS
                }
            Game(board.white, board.black, outcome)
        }
    return TeamMatch(pairing.first, pairing.second, games)
}
