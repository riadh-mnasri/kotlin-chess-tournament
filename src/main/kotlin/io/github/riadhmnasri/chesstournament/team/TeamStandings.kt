package io.github.riadhmnasri.chesstournament.team

private const val DEFAULT_BYE_MATCH_POINTS = 2.0

/** The two ways a team's result is commonly scored. */
enum class TeamScoreType {
    /** 2 for a won match, 1 for a drawn one, 0 for a lost one. */
    MATCH_POINTS,

    /** The sum of every individual board result. */
    GAME_POINTS,
}

/**
 * How [computeTeamStandings] (and so [pairNextTeamRound]) scores teams.
 *
 * Both scores are always computed; [primary] ranks first and the other
 * one is the first tie-break. What a bye is worth varies between
 * competitions: by default it counts as a won match ([byeMatchPoints] =
 * 2) with no board points ([byeGamePoints] = 0). These are app-defined
 * defaults, pass your own to follow a specific competition's rules.
 */
data class TeamScoring(
    val primary: TeamScoreType = TeamScoreType.MATCH_POINTS,
    val byeMatchPoints: Double = DEFAULT_BYE_MATCH_POINTS,
    val byeGamePoints: Double = 0.0,
)

/** A team's ranking data after any number of played rounds. */
data class TeamStanding(
    val team: Team,
    val matchPoints: Double,
    val gamePoints: Double,
)

/**
 * Computes the ranked standings for [teams] after [rounds] have been
 * played: by the [TeamScoring.primary] score, then the other score, then
 * average roster rating, then team name so the order is always
 * deterministic. Team-level tie-breaks such as Sonneborn-Berger are not
 * implemented yet.
 */
fun computeTeamStandings(
    teams: List<Team>,
    rounds: List<TeamRound>,
    scoring: TeamScoring = TeamScoring(),
): List<TeamStanding> {
    val standings =
        teams.map { team ->
            val matches = rounds.flatMap { it.matches }.filter { it.involves(team) }
            val byes = rounds.count { it.byeTeam == team }
            TeamStanding(
                team = team,
                matchPoints = matches.sumOf { it.matchPointsFor(team) } + byes * scoring.byeMatchPoints,
                gamePoints = matches.sumOf { it.gamePointsFor(team) } + byes * scoring.byeGamePoints,
            )
        }

    val primaryThenSecondary =
        when (scoring.primary) {
            TeamScoreType.MATCH_POINTS ->
                compareByDescending<TeamStanding> { it.matchPoints }.thenByDescending { it.gamePoints }
            TeamScoreType.GAME_POINTS ->
                compareByDescending<TeamStanding> { it.gamePoints }.thenByDescending { it.matchPoints }
        }
    return standings.sortedWith(
        primaryThenSecondary
            .thenByDescending { it.team.averageRating() }
            .thenBy { it.team.name },
    )
}
