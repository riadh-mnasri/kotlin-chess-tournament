package io.github.riadhmnasri.chesstournament.rating

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KFactorTest {
    @Test
    fun `a junior below 18 with a rating under 2300 gets the highest K-factor`() {
        // Given
        val age = 15
        val rating = 1800

        // When
        val kFactor = kFactorFor(rating, age)

        // Then
        assertThat(kFactor).isEqualTo(40)
    }

    @Test
    fun `an adult with a rating under 2300 gets the standard K-factor`() {
        // Given
        val age = 30
        val rating = 1800

        // When
        val kFactor = kFactorFor(rating, age)

        // Then
        assertThat(kFactor).isEqualTo(20)
    }

    @Test
    fun `a player with an unknown age and a rating under 2300 gets the standard K-factor`() {
        // Given
        val rating = 1800

        // When
        val kFactor = kFactorFor(rating)

        // Then
        assertThat(kFactor).isEqualTo(20)
    }

    @Test
    fun `a player rated 2400 or above always gets the lowest K-factor`() {
        // Given
        val age = 16
        val rating = 2450

        // When
        val kFactor = kFactorFor(rating, age)

        // Then
        assertThat(kFactor).isEqualTo(10)
    }

    @Test
    fun `a junior above the 2300 rating cutoff does not get the junior K-factor`() {
        // Given
        val age = 16
        val rating = 2350

        // When
        val kFactor = kFactorFor(rating, age)

        // Then
        assertThat(kFactor).isEqualTo(20)
    }
}
