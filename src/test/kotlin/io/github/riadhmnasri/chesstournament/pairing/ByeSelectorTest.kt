package io.github.riadhmnasri.chesstournament.pairing

import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ByeSelectorTest {
    private val alice = Player(id = "1", name = "Alice", rating = 2400)
    private val bob = Player(id = "2", name = "Bob", rating = 2300)
    private val charlie = Player(id = "3", name = "Charlie", rating = 2200)

    @Test
    fun `returns null when the field has an even number of players`() {
        // Given
        val rankedPlayers = listOf(alice, bob)

        // When
        val byePlayer = selectByePlayer(rankedPlayers, previousRounds = emptyList())

        // Then
        assertThat(byePlayer).isNull()
    }

    @Test
    fun `picks the lowest ranked player when nobody has had a bye yet`() {
        // Given
        val rankedPlayers = listOf(alice, bob, charlie)

        // When
        val byePlayer = selectByePlayer(rankedPlayers, previousRounds = emptyList())

        // Then
        assertThat(byePlayer).isEqualTo(charlie)
    }

    @Test
    fun `skips a player who has already had a bye, even if they are the lowest ranked`() {
        // Given: Charlie already had a bye in round 1
        val rankedPlayers = listOf(alice, bob, charlie)
        val previousRounds = listOf(Round(number = 1, games = emptyList(), byePlayer = charlie))

        // When
        val byePlayer = selectByePlayer(rankedPlayers, previousRounds)

        // Then
        assertThat(byePlayer).isEqualTo(bob)
    }
}
