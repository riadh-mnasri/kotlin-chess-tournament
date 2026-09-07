package io.github.riadhmnasri.chesstournament.standings

import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.GameOutcome
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

class BuchholzCut1Test {
    @Test
    fun `buchholz cut-1 excludes the single lowest-scoring opponent`() {
        // Given: Alice faced Bob (1.0), Charlie (2.0) and Dave (0.5)
        val alice = Player(id = "a", name = "Alice", rating = 2000)
        val bob = Player(id = "b", name = "Bob", rating = 1900)
        val charlie = Player(id = "c", name = "Charlie", rating = 1800)
        val dave = Player(id = "d", name = "Dave", rating = 1700)
        val rounds =
            listOf(
                Round(number = 1, games = listOf(Game(alice, bob, GameOutcome.WHITE_WINS))),
                Round(number = 2, games = listOf(Game(alice, charlie, GameOutcome.WHITE_WINS))),
                Round(number = 3, games = listOf(Game(alice, dave, GameOutcome.WHITE_WINS))),
            )
        val scoreByPlayer = mapOf(alice to 3.0, bob to 1.0, charlie to 2.0, dave to 0.5)

        // When
        val buchholzCut1ByPlayer = buchholzCut1(listOf(alice), rounds, scoreByPlayer)

        // Then: plain Buchholz would be 1.0 + 2.0 + 0.5 = 3.5, cut-1 drops
        // Dave's 0.5 (the lowest), leaving 1.0 + 2.0 = 3.0
        assertThat(buchholzCut1ByPlayer.getValue(alice)).isCloseTo(3.0, Offset.offset(0.0001))
    }

    @Test
    fun `only one instance of the lowest opponent score is excluded when there is a tie for lowest`() {
        // Given: Alice faced Bob (1.0) and Charlie (1.0), tied for lowest, plus Dave (2.0)
        val alice = Player(id = "a", name = "Alice", rating = 2000)
        val bob = Player(id = "b", name = "Bob", rating = 1900)
        val charlie = Player(id = "c", name = "Charlie", rating = 1800)
        val dave = Player(id = "d", name = "Dave", rating = 1700)
        val rounds =
            listOf(
                Round(number = 1, games = listOf(Game(alice, bob, GameOutcome.WHITE_WINS))),
                Round(number = 2, games = listOf(Game(alice, charlie, GameOutcome.WHITE_WINS))),
                Round(number = 3, games = listOf(Game(alice, dave, GameOutcome.WHITE_WINS))),
            )
        val scoreByPlayer = mapOf(alice to 3.0, bob to 1.0, charlie to 1.0, dave to 2.0)

        // When
        val buchholzCut1ByPlayer = buchholzCut1(listOf(alice), rounds, scoreByPlayer)

        // Then: only one of the two tied 1.0 scores is dropped, leaving 1.0 + 2.0 = 3.0
        assertThat(buchholzCut1ByPlayer.getValue(alice)).isCloseTo(3.0, Offset.offset(0.0001))
    }

    @Test
    fun `a player with no real opponents has nothing to cut`() {
        // Given: a single player who only ever gets byes
        val soloPlayer = Player(id = "a", name = "Alice", rating = 2000)
        val rounds = listOf(Round(number = 1, games = emptyList(), byePlayer = soloPlayer))
        val scoreByPlayer = mapOf(soloPlayer to 1.0)

        // When
        val buchholzCut1ByPlayer = buchholzCut1(listOf(soloPlayer), rounds, scoreByPlayer)

        // Then
        assertThat(buchholzCut1ByPlayer.getValue(soloPlayer)).isCloseTo(0.0, Offset.offset(0.0001))
    }

    @Test
    fun `a player with a single real opponent has nothing to cut, same as plain buchholz`() {
        // Given: Alice's only game is against Bob
        val alice = Player(id = "a", name = "Alice", rating = 2000)
        val bob = Player(id = "b", name = "Bob", rating = 1900)
        val rounds = listOf(Round(number = 1, games = listOf(Game(alice, bob, GameOutcome.WHITE_WINS))))
        val scoreByPlayer = mapOf(alice to 1.0, bob to 0.0)

        // When
        val buchholzCut1ByPlayer = buchholzCut1(listOf(alice), rounds, scoreByPlayer)
        val plainBuchholzByPlayer = buchholz(listOf(alice), rounds, scoreByPlayer)

        // Then
        assertThat(buchholzCut1ByPlayer.getValue(alice)).isEqualTo(plainBuchholzByPlayer.getValue(alice))
    }
}
