package io.github.riadhmnasri.chesstournament.pairing

import io.github.riadhmnasri.chesstournament.model.Player
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

class AcceleratedPairingsTest {
    private val first = Player(id = "1", name = "First", rating = 2400)
    private val second = Player(id = "2", name = "Second", rating = 2300)
    private val third = Player(id = "3", name = "Third", rating = 2200)
    private val fourth = Player(id = "4", name = "Fourth", rating = 2100)

    @Test
    fun `the top half of the field gets one virtual point in round 1`() {
        // Given
        val players = listOf(first, second, third, fourth)

        // When
        val virtualPoints = acceleratedVirtualPoints(players, roundNumber = 1)

        // Then
        assertThat(virtualPoints.getValue(first)).isCloseTo(1.0, Offset.offset(0.0001))
        assertThat(virtualPoints.getValue(second)).isCloseTo(1.0, Offset.offset(0.0001))
        assertThat(virtualPoints.getValue(third)).isCloseTo(0.0, Offset.offset(0.0001))
        assertThat(virtualPoints.getValue(fourth)).isCloseTo(0.0, Offset.offset(0.0001))
    }

    @Test
    fun `the top half of the field gets half a virtual point in round 2`() {
        // Given
        val players = listOf(first, second, third, fourth)

        // When
        val virtualPoints = acceleratedVirtualPoints(players, roundNumber = 2)

        // Then
        assertThat(virtualPoints.getValue(first)).isCloseTo(0.5, Offset.offset(0.0001))
        assertThat(virtualPoints.getValue(second)).isCloseTo(0.5, Offset.offset(0.0001))
        assertThat(virtualPoints.getValue(third)).isCloseTo(0.0, Offset.offset(0.0001))
        assertThat(virtualPoints.getValue(fourth)).isCloseTo(0.0, Offset.offset(0.0001))
    }

    @Test
    fun `there is no more acceleration from round 3 onward`() {
        // Given
        val players = listOf(first, second, third, fourth)

        // When
        val virtualPoints = acceleratedVirtualPoints(players, roundNumber = 3)

        // Then
        assertThat(virtualPoints.values).allMatch { it == 0.0 }
    }

    @Test
    fun `an odd field splits the extra player into the bottom, unaccelerated half`() {
        // Given: five players, so the top half is the top two by rating
        val fifth = Player(id = "5", name = "Fifth", rating = 2000)
        val players = listOf(first, second, third, fourth, fifth)

        // When
        val virtualPoints = acceleratedVirtualPoints(players, roundNumber = 1)

        // Then
        assertThat(virtualPoints.getValue(first)).isCloseTo(1.0, Offset.offset(0.0001))
        assertThat(virtualPoints.getValue(second)).isCloseTo(1.0, Offset.offset(0.0001))
        assertThat(virtualPoints.getValue(third)).isCloseTo(0.0, Offset.offset(0.0001))
        assertThat(virtualPoints.getValue(fourth)).isCloseTo(0.0, Offset.offset(0.0001))
        assertThat(virtualPoints.getValue(fifth)).isCloseTo(0.0, Offset.offset(0.0001))
    }
}
