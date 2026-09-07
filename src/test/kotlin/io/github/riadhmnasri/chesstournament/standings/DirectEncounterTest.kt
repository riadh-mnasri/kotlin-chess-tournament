package io.github.riadhmnasri.chesstournament.standings

import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.GameOutcome
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

class DirectEncounterTest {
    private val alice = Player(id = "a", name = "Alice", rating = 2000)
    private val bob = Player(id = "b", name = "Bob", rating = 1900)
    private val charlie = Player(id = "c", name = "Charlie", rating = 1800)

    @Test
    fun `within a fully played mini round-robin, the winner of the head-to-head gets the higher mini-score`() {
        // Given: a group of Alice and Bob who played each other exactly once, Alice won
        val rounds = listOf(Round(number = 1, games = listOf(Game(alice, bob, GameOutcome.WHITE_WINS))))
        val group = listOf(alice, bob)

        // When
        val directEncounterByPlayer = directEncounter(group, rounds)

        // Then
        assertThat(directEncounterByPlayer.getValue(alice)).isCloseTo(1.0, Offset.offset(0.0001))
        assertThat(directEncounterByPlayer.getValue(bob)).isCloseTo(0.0, Offset.offset(0.0001))
    }

    @Test
    fun `a three-player cyclic result gives every player an equal mini-score`() {
        // Given: a fully played mini round-robin of 3 with a cycle:
        // Alice beats Bob, Bob beats Charlie, Charlie beats Alice
        val rounds =
            listOf(
                Round(number = 1, games = listOf(Game(alice, bob, GameOutcome.WHITE_WINS)), byePlayer = charlie),
                Round(number = 2, games = listOf(Game(bob, charlie, GameOutcome.WHITE_WINS)), byePlayer = alice),
                Round(number = 3, games = listOf(Game(charlie, alice, GameOutcome.WHITE_WINS)), byePlayer = bob),
            )
        val group = listOf(alice, bob, charlie)

        // When
        val directEncounterByPlayer = directEncounter(group, rounds)

        // Then: each player won exactly one of their two games within the group
        assertThat(directEncounterByPlayer.getValue(alice)).isCloseTo(1.0, Offset.offset(0.0001))
        assertThat(directEncounterByPlayer.getValue(bob)).isCloseTo(1.0, Offset.offset(0.0001))
        assertThat(directEncounterByPlayer.getValue(charlie)).isCloseTo(1.0, Offset.offset(0.0001))
    }

    @Test
    fun `a group that is not a complete mini round-robin gets a neutral mini-score for everyone`() {
        // Given: a group of 3 where Charlie never played either Alice or Bob
        val rounds =
            listOf(Round(number = 1, games = listOf(Game(alice, bob, GameOutcome.WHITE_WINS)), byePlayer = charlie))
        val group = listOf(alice, bob, charlie)

        // When
        val directEncounterByPlayer = directEncounter(group, rounds)

        // Then
        assertThat(directEncounterByPlayer.getValue(alice)).isCloseTo(0.0, Offset.offset(0.0001))
        assertThat(directEncounterByPlayer.getValue(bob)).isCloseTo(0.0, Offset.offset(0.0001))
        assertThat(directEncounterByPlayer.getValue(charlie)).isCloseTo(0.0, Offset.offset(0.0001))
    }

    @Test
    fun `a pair that played each other twice is not a complete mini round-robin either`() {
        // Given: Alice and Bob played twice (e.g. a double round-robin section)
        val rounds =
            listOf(
                Round(number = 1, games = listOf(Game(alice, bob, GameOutcome.WHITE_WINS)), byePlayer = null),
                Round(number = 2, games = listOf(Game(bob, alice, GameOutcome.WHITE_WINS)), byePlayer = null),
            )
        val group = listOf(alice, bob)

        // When
        val directEncounterByPlayer = directEncounter(group, rounds)

        // Then
        assertThat(directEncounterByPlayer.getValue(alice)).isCloseTo(0.0, Offset.offset(0.0001))
        assertThat(directEncounterByPlayer.getValue(bob)).isCloseTo(0.0, Offset.offset(0.0001))
    }

    @Test
    fun `games and byes against players outside the group do not count toward the mini-score`() {
        // Given: a group of just Alice and Bob, plus an outsider Charlie who also played Alice
        val rounds =
            listOf(
                Round(number = 1, games = listOf(Game(alice, bob, GameOutcome.DRAW)), byePlayer = charlie),
                Round(number = 2, games = listOf(Game(alice, charlie, GameOutcome.WHITE_WINS)), byePlayer = bob),
            )
        val group = listOf(alice, bob)

        // When
        val directEncounterByPlayer = directEncounter(group, rounds)

        // Then: only the round-1 draw between Alice and Bob counts
        assertThat(directEncounterByPlayer.getValue(alice)).isCloseTo(0.5, Offset.offset(0.0001))
        assertThat(directEncounterByPlayer.getValue(bob)).isCloseTo(0.5, Offset.offset(0.0001))
    }

    @Test
    fun `a group of a single player has no head-to-head and gets a neutral mini-score`() {
        // Given
        val rounds = listOf(Round(number = 1, games = emptyList(), byePlayer = alice))
        val group = listOf(alice)

        // When
        val directEncounterByPlayer = directEncounter(group, rounds)

        // Then
        assertThat(directEncounterByPlayer.getValue(alice)).isCloseTo(0.0, Offset.offset(0.0001))
    }
}
