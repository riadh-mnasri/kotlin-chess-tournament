package io.github.riadhmnasri.chesstournament.standings

import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.GameOutcome
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NumberOfWinsTest {
    @Test
    fun `counts only decisive games won, not draws or losses`() {
        // Given: Alice wins round 1, draws round 2, loses round 3
        val alice = Player(id = "a", name = "Alice", rating = 2000)
        val bob = Player(id = "b", name = "Bob", rating = 1900)
        val charlie = Player(id = "c", name = "Charlie", rating = 1800)
        val dave = Player(id = "d", name = "Dave", rating = 1700)
        val rounds =
            listOf(
                Round(number = 1, games = listOf(Game(alice, bob, GameOutcome.WHITE_WINS))),
                Round(number = 2, games = listOf(Game(alice, charlie, GameOutcome.DRAW))),
                Round(number = 3, games = listOf(Game(dave, alice, GameOutcome.WHITE_WINS))),
            )

        // When
        val winsByPlayer = numberOfWins(listOf(alice), rounds)

        // Then
        assertThat(winsByPlayer.getValue(alice)).isEqualTo(1)
    }

    @Test
    fun `a player with only draws has zero wins`() {
        // Given
        val alice = Player(id = "a", name = "Alice", rating = 2000)
        val bob = Player(id = "b", name = "Bob", rating = 1900)
        val rounds = listOf(Round(number = 1, games = listOf(Game(alice, bob, GameOutcome.DRAW))))

        // When
        val winsByPlayer = numberOfWins(listOf(alice), rounds)

        // Then
        assertThat(winsByPlayer.getValue(alice)).isEqualTo(0)
    }

    @Test
    fun `a bye does not count as a win`() {
        // Given: Alice's only "result" this round is a bye, not a real win
        val alice = Player(id = "a", name = "Alice", rating = 2000)
        val rounds = listOf(Round(number = 1, games = emptyList(), byePlayer = alice))

        // When
        val winsByPlayer = numberOfWins(listOf(alice), rounds)

        // Then
        assertThat(winsByPlayer.getValue(alice)).isEqualTo(0)
    }

    @Test
    fun `counts a win whether the player had white or black`() {
        // Given: Alice wins as white in round 1, and as black in round 2
        val alice = Player(id = "a", name = "Alice", rating = 2000)
        val bob = Player(id = "b", name = "Bob", rating = 1900)
        val rounds =
            listOf(
                Round(number = 1, games = listOf(Game(alice, bob, GameOutcome.WHITE_WINS))),
                Round(number = 2, games = listOf(Game(bob, alice, GameOutcome.BLACK_WINS))),
            )

        // When
        val winsByPlayer = numberOfWins(listOf(alice), rounds)

        // Then
        assertThat(winsByPlayer.getValue(alice)).isEqualTo(2)
    }
}
