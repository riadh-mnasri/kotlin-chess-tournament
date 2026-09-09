package io.github.riadhmnasri.chesstournament.pairing

import io.github.riadhmnasri.chesstournament.model.Player
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

private fun playersRatedDescending(count: Int): List<Player> =
    (1..count).map { rank -> Player(id = "$rank", name = "Player $rank", rating = 2500 - rank) }

class BakuAccelerationTest {
    @Test
    fun `in a 9 round tournament, GA gets a full virtual point in rounds 1 to 3`() {
        // Given: the official FIDE-documented example (9 rounds, 5 accelerated: 3 full, 2 half)
        val players = playersRatedDescending(20)

        // When / Then
        listOf(1, 2, 3).forEach { round ->
            val virtualPoints = bakuAcceleratedVirtualPoints(players, roundNumber = round, totalRounds = 9)
            assertThat(virtualPoints.getValue(players.first())).isCloseTo(1.0, Offset.offset(0.0001))
        }
    }

    @Test
    fun `in a 9 round tournament, GA gets half a virtual point in rounds 4 and 5`() {
        // Given
        val players = playersRatedDescending(20)

        // When / Then
        listOf(4, 5).forEach { round ->
            val virtualPoints = bakuAcceleratedVirtualPoints(players, roundNumber = round, totalRounds = 9)
            assertThat(virtualPoints.getValue(players.first())).isCloseTo(0.5, Offset.offset(0.0001))
        }
    }

    @Test
    fun `in a 9 round tournament, no virtual points are given from round 6 onward`() {
        // Given
        val players = playersRatedDescending(20)

        // When / Then
        (6..9).forEach { round ->
            val virtualPoints = bakuAcceleratedVirtualPoints(players, roundNumber = round, totalRounds = 9)
            assertThat(virtualPoints.values).allMatch { it == 0.0 }
        }
    }

    @Test
    fun `GB never receives virtual points during the accelerated rounds`() {
        // Given: 20 players, GA size = 2 * ceil(20/4) = 10, so players 11 to 20 are GB
        val players = playersRatedDescending(20)

        // When
        val virtualPoints = bakuAcceleratedVirtualPoints(players, roundNumber = 1, totalRounds = 9)

        // Then
        players.drop(10).forEach { player -> assertThat(virtualPoints.getValue(player)).isEqualTo(0.0) }
    }

    @Test
    fun `GA size follows 2 times the participant count divided by 4 and rounded up`() {
        // Given: 10 players -> Q = ceil(10/4) = 3 -> GA size = 6
        val players = playersRatedDescending(10)

        // When
        val virtualPoints = bakuAcceleratedVirtualPoints(players, roundNumber = 1, totalRounds = 9)

        // Then: the top 6 rated players are in GA, the bottom 4 are not
        val gaPlayers = players.take(6)
        val gbPlayers = players.drop(6)
        gaPlayers.forEach { assertThat(virtualPoints.getValue(it)).isCloseTo(1.0, Offset.offset(0.0001)) }
        gbPlayers.forEach { assertThat(virtualPoints.getValue(it)).isEqualTo(0.0) }
    }
}
