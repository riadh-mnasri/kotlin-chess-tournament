package io.github.riadhmnasri.chesstournament.team

import io.github.riadhmnasri.chesstournament.model.Pairing
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test

class TeamPairingTest {
    private val alpha = team("A", 2400, 2300, 2200, 2100)
    private val beta = team("B", 2300, 2200, 2100, 2000)
    private val gamma = team("C", 2200, 2100, 2000, 1900)
    private val delta = team("D", 2100, 2000, 1900, 1800)
    private val teams = listOf(alpha, beta, gamma, delta)

    @Test
    fun `the team listed first plays white on odd boards and black on even boards`() {
        // When
        val boards = TeamPairing(alpha, beta).boards()

        // Then
        assertThat(boards).containsExactly(
            Pairing(white = alpha.players[0], black = beta.players[0]),
            Pairing(white = beta.players[1], black = alpha.players[1]),
            Pairing(white = alpha.players[2], black = beta.players[2]),
            Pairing(white = beta.players[3], black = alpha.players[3]),
        )
    }

    @Test
    fun `a lineup can field reserves but must stay within its own roster`() {
        // Given: a five-player roster fielding boards 1 to 3 and its reserve
        val withReserve = Team("E", "Team E", alpha.players + team("R", 1700).players)
        val lineup = listOf(withReserve.players[0], withReserve.players[1], withReserve.players[4])

        // Then
        assertThat(TeamPairing(withReserve, beta).boards(lineup, beta.players.take(3))).hasSize(3)
        assertThatIllegalArgumentException().isThrownBy {
            TeamPairing(alpha, beta).boards(beta.players.take(2), beta.players.take(2))
        }
        assertThatIllegalArgumentException().isThrownBy {
            TeamPairing(alpha, beta).boards(alpha.players.take(3), beta.players.take(2))
        }
    }

    @Test
    fun `first round pairs the top half of the field against the bottom half`() {
        // When
        val result = pairFirstTeamRound(teams.shuffled(java.util.Random(7)))

        // Then: listed-first alternates between pairings, as for individual colors
        assertThat(result.byeTeam).isNull()
        assertThat(result.pairings).containsExactly(TeamPairing(alpha, gamma), TeamPairing(delta, beta))
    }

    @Test
    fun `with an odd number of teams, the lowest-rated one gets the first round bye`() {
        // When
        val result = pairFirstTeamRound(listOf(alpha, beta, gamma))

        // Then
        assertThat(result.byeTeam).isEqualTo(gamma)
        assertThat(result.pairings).containsExactly(TeamPairing(alpha, beta))
    }

    @Test
    fun `two teams that already met are not paired again`() {
        // Given: alpha and gamma both won round 1, so they share the top score group
        val round1 =
            TeamRound(
                1,
                listOf(
                    play(TeamPairing(alpha, beta), 1.0, 1.0, 1.0, 0.5),
                    play(TeamPairing(gamma, delta), 1.0, 1.0, 0.5, 0.5),
                ),
            )

        // When
        val round2 = pairNextTeamRound(teams, listOf(round1))

        // Then: the winners meet, and so do the losers
        val matchups = round2.pairings.map { setOf(it.first, it.second) }
        assertThat(matchups).containsExactlyInAnyOrder(setOf(alpha, gamma), setOf(beta, delta))

        // And: no team meets its round 1 opponent in round 3 either
        val round2Played =
            TeamRound(2, round2.pairings.map { play(it, 0.5, 0.5, 0.5, 0.5) })
        val round3 = pairNextTeamRound(teams, listOf(round1, round2Played))
        assertThat(round3.pairings.map { setOf(it.first, it.second) })
            .doesNotContain(setOf(alpha, beta), setOf(gamma, delta), setOf(alpha, gamma), setOf(beta, delta))
    }

    @Test
    fun `the team listed second last round is listed first next`() {
        // Given: alpha was listed first in round 1, gamma second
        val round1 =
            TeamRound(
                1,
                listOf(
                    play(TeamPairing(alpha, beta), 1.0, 1.0, 1.0, 0.5),
                    play(TeamPairing(delta, gamma), 0.0, 0.0, 0.5, 0.5),
                ),
            )

        // When: the two winners, alpha and gamma, meet
        val round2 = pairNextTeamRound(teams, listOf(round1))

        // Then: gamma, listed second before, is listed first now
        assertThat(round2.pairings).contains(TeamPairing(gamma, alpha))
    }

    @Test
    fun `the bye rotates to a team that has not had one yet`() {
        // Given: three teams, gamma sat out round 1 and then scored nothing
        val threeTeams = listOf(alpha, beta, gamma)
        val round1 = TeamRound(1, listOf(play(TeamPairing(alpha, beta), 1.0, 1.0, 1.0, 1.0)), byeTeam = gamma)

        // When
        val round2 = pairNextTeamRound(threeTeams, listOf(round1), TeamScoring(byeMatchPoints = 0.0))

        // Then: gamma is last but already had a bye, so beta gets it
        assertThat(round2.byeTeam).isEqualTo(beta)
        assertThat(round2.pairings.single().let { setOf(it.first, it.second) }).isEqualTo(setOf(alpha, gamma))
    }
}
