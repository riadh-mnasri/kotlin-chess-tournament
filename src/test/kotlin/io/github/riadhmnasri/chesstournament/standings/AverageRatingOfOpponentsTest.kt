package io.github.riadhmnasri.chesstournament.standings

import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.GameOutcome
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

class AverageRatingOfOpponentsTest {
    @Test
    fun `average rating of opponents is the mean rating of the opponents a player actually faced`() {
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

        // When
        val averageRatingByPlayer = averageRatingOfOpponents(listOf(alice, bob, charlie), rounds)

        // Then: Alice faced Bob (1900) and Charlie (1800) -> mean 1850
        //       Bob and Charlie each only faced Alice (2000)
        assertThat(averageRatingByPlayer.getValue(alice)).isCloseTo(1850.0, Offset.offset(0.001))
        assertThat(averageRatingByPlayer.getValue(bob)).isCloseTo(2000.0, Offset.offset(0.001))
        assertThat(averageRatingByPlayer.getValue(charlie)).isCloseTo(2000.0, Offset.offset(0.001))
    }

    @Test
    fun `a player who has only had byes has an average rating of opponents of zero`() {
        // Given: a single player who only ever gets byes
        val soloPlayer = Player(id = "a", name = "Alice", rating = 2000)
        val rounds = listOf(Round(number = 1, games = emptyList(), byePlayer = soloPlayer))

        // When
        val averageRatingByPlayer = averageRatingOfOpponents(listOf(soloPlayer), rounds)

        // Then
        assertThat(averageRatingByPlayer.getValue(soloPlayer)).isCloseTo(0.0, Offset.offset(0.001))
    }
}
