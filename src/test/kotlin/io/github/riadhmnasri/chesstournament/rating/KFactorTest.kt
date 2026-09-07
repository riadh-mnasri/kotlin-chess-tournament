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

    @Test
    fun `a player new to the rating list with fewer than 30 rated games gets the highest K-factor`() {
        // Given
        val age = 30
        val rating = 1800
        val ratedGamesPlayed = 12

        // When
        val kFactor = kFactorFor(rating, age, ratedGamesPlayed)

        // Then
        assertThat(kFactor).isEqualTo(40)
    }

    @Test
    fun `a player new to the rating list gets the highest K-factor even when rated 2400 or above`() {
        // Given
        val age = 30
        val rating = 2450
        val ratedGamesPlayed = 5

        // When
        val kFactor = kFactorFor(rating, age, ratedGamesPlayed)

        // Then
        assertThat(kFactor).isEqualTo(40)
    }

    @Test
    fun `a player with 30 or more rated games is no longer treated as new to the rating list`() {
        // Given
        val age = 30
        val rating = 1800
        val ratedGamesPlayed = 30

        // When
        val kFactor = kFactorFor(rating, age, ratedGamesPlayed)

        // Then
        assertThat(kFactor).isEqualTo(20)
    }

    @Test
    fun `an unknown rated game count does not trigger the new-player K-factor`() {
        // Given
        val age = 30
        val rating = 1800

        // When
        val kFactor = kFactorFor(rating, age)

        // Then
        assertThat(kFactor).isEqualTo(20)
    }

    @Test
    fun `a player who has ever reached 2400 keeps the lowest K-factor even after dropping back below it`() {
        // Given
        val age = 30
        val rating = 2350

        // When
        val kFactor = kFactorFor(rating, age, hasEverReachedTopRating = true)

        // Then
        assertThat(kFactor).isEqualTo(10)
    }

    @Test
    fun `a player currently at or above 2400 gets the lowest K-factor regardless of the lifetime flag`() {
        // Given
        val age = 30
        val rating = 2450

        // When
        val kFactor = kFactorFor(rating, age, hasEverReachedTopRating = false)

        // Then
        assertThat(kFactor).isEqualTo(10)
    }

    @Test
    fun `the lifetime top-rating flag defaults to false and does not change existing behavior`() {
        // Given
        val age = 30
        val rating = 2350

        // When
        val kFactor = kFactorFor(rating, age)

        // Then
        assertThat(kFactor).isEqualTo(20)
    }

    @Test
    fun `a new player takes priority over a stale lifetime top-rating flag`() {
        // Given: fewer than 30 rated games, even if somehow flagged as having once reached 2400
        val age = 30
        val rating = 1800
        val ratedGamesPlayed = 5

        // When
        val kFactor = kFactorFor(rating, age, ratedGamesPlayed, hasEverReachedTopRating = true)

        // Then
        assertThat(kFactor).isEqualTo(40)
    }
}
