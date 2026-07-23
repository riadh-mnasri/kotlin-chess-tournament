package io.github.riadhmnasri.chesstournament.standings

import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.GameOutcome
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

class SonnebornBergerTest {
    @Test
    fun `a win counts the opponent full score, a draw counts it halved, a loss counts nothing`() {
        // Given: Alice beats Bob in round 1 and draws Charlie in round 2,
        // Charlie has a bye in round 1 and Bob has a bye in round 2
        val alice = Player(id = "a", name = "Alice", rating = 2000)
        val bob = Player(id = "b", name = "Bob", rating = 1900)
        val charlie = Player(id = "c", name = "Charlie", rating = 1800)
        val rounds =
            listOf(
                Round(
                    number = 1,
                    games = listOf(Game(alice, bob, GameOutcome.WHITE_WINS)),
                    byePlayer = charlie,
                ),
                Round(
                    number = 2,
                    games = listOf(Game(alice, charlie, GameOutcome.DRAW)),
                    byePlayer = bob,
                ),
            )
        // Final scores: Alice 1.5, Bob 1.0, Charlie 1.5
        val scoreByPlayer = mapOf(alice to 1.5, bob to 1.0, charlie to 1.5)

        // When
        val sonnebornBergerByPlayer = sonnebornBerger(listOf(alice, bob, charlie), rounds, scoreByPlayer)

        // Then: Alice = full Bob score (1.0) + half Charlie score (0.75) = 1.75
        //       Bob lost his only game -> 0
        //       Charlie only drew -> half Alice score (0.75)
        assertThat(sonnebornBergerByPlayer.getValue(alice)).isCloseTo(1.75, Offset.offset(0.0001))
        assertThat(sonnebornBergerByPlayer.getValue(bob)).isCloseTo(0.0, Offset.offset(0.0001))
        assertThat(sonnebornBergerByPlayer.getValue(charlie)).isCloseTo(0.75, Offset.offset(0.0001))
    }
}
