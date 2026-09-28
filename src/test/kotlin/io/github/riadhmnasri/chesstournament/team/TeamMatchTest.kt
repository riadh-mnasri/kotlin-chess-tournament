package io.github.riadhmnasri.chesstournament.team

import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.GameOutcome
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test

class TeamMatchTest {
    private val alpha = team("A", 2200, 2100, 2000, 1900)
    private val beta = team("B", 2150, 2050, 1950, 1850)

    @Test
    fun `game points are the sum of each team's individual board results`() {
        // Given: alpha wins two boards, draws one and loses one
        val match = play(TeamPairing(alpha, beta), 1.0, 1.0, 0.5, 0.0)

        // Then
        assertThat(match.gamePointsFor(alpha)).isEqualTo(2.5)
        assertThat(match.gamePointsFor(beta)).isEqualTo(1.5)
    }

    @Test
    fun `match points are 2 for the winner and 0 for the loser`() {
        // Given
        val match = play(TeamPairing(alpha, beta), 1.0, 1.0, 0.5, 0.0)

        // Then
        assertThat(match.matchPointsFor(alpha)).isEqualTo(2.0)
        assertThat(match.matchPointsFor(beta)).isEqualTo(0.0)
    }

    @Test
    fun `a drawn match gives both teams 1 match point`() {
        // Given: 2-2 on four boards
        val match = play(TeamPairing(alpha, beta), 1.0, 0.0, 0.5, 0.5)

        // Then
        assertThat(match.matchPointsFor(alpha)).isEqualTo(1.0)
        assertThat(match.matchPointsFor(beta)).isEqualTo(1.0)
    }

    @Test
    fun `a board game between two players of the same team is rejected`() {
        val game = Game(alpha.players[0], alpha.players[1], GameOutcome.DRAW)

        assertThatIllegalArgumentException().isThrownBy { TeamMatch(alpha, beta, listOf(game)) }
    }

    @Test
    fun `two teams sharing a player cannot meet`() {
        val borrowing = Team("C", "Team C", listOf(alpha.players[0], beta.players[0]))

        assertThatIllegalArgumentException().isThrownBy {
            TeamMatch(alpha, borrowing, listOf(Game(alpha.players[1], beta.players[0], GameOutcome.DRAW)))
        }
    }

    @Test
    fun `a roster cannot be empty or list the same player twice`() {
        assertThatIllegalArgumentException().isThrownBy { Team("C", "Team C", emptyList()) }
        assertThatIllegalArgumentException().isThrownBy {
            Team("C", "Team C", listOf(alpha.players[0], alpha.players[0]))
        }
    }
}
