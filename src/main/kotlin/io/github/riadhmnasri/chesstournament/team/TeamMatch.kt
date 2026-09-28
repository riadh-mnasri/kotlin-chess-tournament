package io.github.riadhmnasri.chesstournament.team

import io.github.riadhmnasri.chesstournament.model.Game

/**
 * A finished match between two teams: one [Game] per board, board 1
 * first. Every game must oppose one player from each roster.
 *
 * [first] is the team listed first, the one playing white on odd boards
 * (see [TeamPairing.boards]); the order only matters for color balance
 * across rounds, not for scoring.
 */
data class TeamMatch(
    val first: Team,
    val second: Team,
    val games: List<Game>,
) {
    init {
        require(first.id != second.id) { "A team cannot play a match against itself (\"${first.id}\")" }
        require(first.players.none { it in second.players }) {
            "Teams \"${first.id}\" and \"${second.id}\" share at least one player"
        }
        require(games.isNotEmpty()) { "A team match needs at least one board game" }
        games.forEachIndexed { index, game ->
            val opposesBothTeams =
                (game.white in first.players && game.black in second.players) ||
                    (game.white in second.players && game.black in first.players)
            require(opposesBothTeams) {
                "Board ${index + 1} must oppose a player of \"${first.id}\" and a player of \"${second.id}\""
            }
        }
    }

    /** The team's board points in this match: the sum of its players' individual results. */
    fun gamePointsFor(team: Team): Double {
        require(team == first || team == second) { "Team \"${team.id}\" did not play this match" }
        return games.sumOf { game -> team.players.sumOf { player -> game.pointsFor(player) } }
    }

    /** The team's match points: 2 for more board points than the opponent, 1 for as many, 0 for fewer. */
    fun matchPointsFor(team: Team): Double {
        val own = gamePointsFor(team)
        val opponent = gamePointsFor(opponentOf(team))
        return when {
            own > opponent -> MATCH_WIN_POINTS
            own == opponent -> MATCH_DRAW_POINTS
            else -> 0.0
        }
    }

    /** The team [team] faced in this match. */
    fun opponentOf(team: Team): Team = if (team == first) second else first

    fun involves(team: Team): Boolean = team == first || team == second

    private companion object {
        const val MATCH_WIN_POINTS = 2.0
        const val MATCH_DRAW_POINTS = 1.0
    }
}

/**
 * One round of a team tournament: its matches, plus the team that sat out
 * this round, if any (`null` when the number of teams is even).
 */
data class TeamRound(
    val number: Int,
    val matches: List<TeamMatch>,
    val byeTeam: Team? = null,
)
