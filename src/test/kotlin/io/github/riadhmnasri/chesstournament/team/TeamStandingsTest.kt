package io.github.riadhmnasri.chesstournament.team

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TeamStandingsTest {
    private val alpha = team("A", 2000, 2000, 2000, 2000)
    private val beta = team("B", 2000, 2000, 2000, 2000)
    private val gamma = team("C", 2000, 2000, 2000, 2000)
    private val delta = team("D", 2000, 2000, 2000, 2000)

    // Alpha scrapes two narrow wins, beta crushes one team and draws the other
    private val rounds =
        listOf(
            TeamRound(
                1,
                listOf(
                    play(TeamPairing(alpha, gamma), 1.0, 1.0, 0.5, 0.0),
                    play(TeamPairing(beta, delta), 1.0, 1.0, 1.0, 1.0),
                ),
            ),
            TeamRound(
                2,
                listOf(
                    play(TeamPairing(alpha, delta), 1.0, 0.5, 0.5, 0.5),
                    play(TeamPairing(beta, gamma), 1.0, 1.0, 0.0, 0.0),
                ),
            ),
        )

    @Test
    fun `under match points, two narrow wins beat a big win and a draw`() {
        // When
        val standings = computeTeamStandings(listOf(alpha, beta, gamma, delta), rounds)

        // Then
        assertThat(standings.map { it.team }.take(2)).containsExactly(alpha, beta)
        assertThat(standings.first().matchPoints).isEqualTo(4.0)
        assertThat(standings.first().gamePoints).isEqualTo(5.0)
    }

    @Test
    fun `under game points, the big win comes first`() {
        // When
        val standings =
            computeTeamStandings(
                listOf(alpha, beta, gamma, delta),
                rounds,
                TeamScoring(primary = TeamScoreType.GAME_POINTS),
            )

        // Then: beta has 6 game points, alpha 5
        assertThat(standings.map { it.team }.take(2)).containsExactly(beta, alpha)
    }

    @Test
    fun `the other score breaks ties on the primary one`() {
        // Given: alpha and beta both win their only match, beta by more boards
        val oneRound =
            listOf(
                TeamRound(
                    1,
                    listOf(
                        play(TeamPairing(alpha, gamma), 1.0, 1.0, 0.5, 0.0),
                        play(TeamPairing(beta, delta), 1.0, 1.0, 1.0, 0.0),
                    ),
                ),
            )

        // When
        val standings = computeTeamStandings(listOf(alpha, beta, gamma, delta), oneRound)

        // Then
        assertThat(standings.map { it.team }.take(2)).containsExactly(beta, alpha)
    }

    @Test
    fun `a bye counts as a won match with no board points by default`() {
        // Given
        val byeRound = listOf(TeamRound(1, listOf(play(TeamPairing(alpha, beta), 1.0, 1.0, 1.0, 1.0)), byeTeam = gamma))

        // When
        val standing = computeTeamStandings(listOf(alpha, beta, gamma), byeRound).single { it.team == gamma }

        // Then
        assertThat(standing.matchPoints).isEqualTo(2.0)
        assertThat(standing.gamePoints).isEqualTo(0.0)
    }

    @Test
    fun `what a bye is worth is configurable`() {
        // Given: a competition that scores a bye as a drawn match worth half the boards
        val byeRound = listOf(TeamRound(1, listOf(play(TeamPairing(alpha, beta), 1.0, 1.0, 1.0, 1.0)), byeTeam = gamma))
        val scoring = TeamScoring(byeMatchPoints = 1.0, byeGamePoints = 2.0)

        // When
        val standing = computeTeamStandings(listOf(alpha, beta, gamma), byeRound, scoring).single { it.team == gamma }

        // Then
        assertThat(standing.matchPoints).isEqualTo(1.0)
        assertThat(standing.gamePoints).isEqualTo(2.0)
    }
}
