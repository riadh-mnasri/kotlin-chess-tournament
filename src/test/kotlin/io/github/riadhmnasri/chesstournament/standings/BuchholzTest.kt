package io.github.riadhmnasri.chesstournament.standings

import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.GameOutcome
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

class BuchholzTest {
    @Test
    fun `buchholz sums the final scores of the opponents a player actually faced`() {
        // Given: Alice plays Bob in round 1 and Charlie in round 2, Charlie
        // has a bye in round 1 and Bob has a bye in round 2
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
        // Final scores: Alice 1.5 (win + draw), Bob 1.0 (loss + bye), Charlie 1.5 (bye + draw)
        val scoreByPlayer = mapOf(alice to 1.5, bob to 1.0, charlie to 1.5)

        // When
        val buchholzByPlayer = buchholz(listOf(alice, bob, charlie), rounds, scoreByPlayer)

        // Then: Alice faced Bob (1.0) and Charlie (1.5) -> 2.5
        //       Bob and Charlie each only faced Alice (1.5) -> 1.5
        assertThat(buchholzByPlayer.getValue(alice)).isCloseTo(2.5, Offset.offset(0.0001))
        assertThat(buchholzByPlayer.getValue(bob)).isCloseTo(1.5, Offset.offset(0.0001))
        assertThat(buchholzByPlayer.getValue(charlie)).isCloseTo(1.5, Offset.offset(0.0001))
    }

    @Test
    fun `a bye does not count as an opponent for buchholz purposes`() {
        // Given: a single player who only ever gets byes
        val soloPlayer = Player(id = "a", name = "Alice", rating = 2000)
        val rounds = listOf(Round(number = 1, games = emptyList(), byePlayer = soloPlayer))
        val scoreByPlayer = mapOf(soloPlayer to 1.0)

        // When
        val buchholzByPlayer = buchholz(listOf(soloPlayer), rounds, scoreByPlayer)

        // Then
        assertThat(buchholzByPlayer.getValue(soloPlayer)).isCloseTo(0.0, Offset.offset(0.0001))
    }
}
