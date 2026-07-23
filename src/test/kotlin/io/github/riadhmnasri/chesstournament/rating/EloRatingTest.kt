package io.github.riadhmnasri.chesstournament.rating

import io.github.riadhmnasri.chesstournament.model.GameResult
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

class EloRatingTest {
    @Test
    fun `expected score is 0-5 when both players have the same rating`() {
        // Given
        val playerRating = 2000
        val opponentRating = 2000

        // When
        val expected = expectedScore(playerRating, opponentRating)

        // Then
        assertThat(expected).isCloseTo(0.5, Offset.offset(0.0001))
    }

    @Test
    fun `expected score favors the higher rated player`() {
        // Given
        val strongerPlayerRating = 2200
        val weakerPlayerRating = 2000

        // When
        val expected = expectedScore(strongerPlayerRating, weakerPlayerRating)

        // Then
        assertThat(expected).isGreaterThan(0.5)
    }

    @Test
    fun `a 400 point rating gap gives the stronger player close to a 91 percent expected score`() {
        // Given: the Elo formula is built so that a 400 point gap
        // corresponds to roughly a ten to one expected outcome
        val playerRating = 2400
        val opponentRating = 2000

        // When
        val expected = expectedScore(playerRating, opponentRating)

        // Then
        assertThat(expected).isCloseTo(0.909, Offset.offset(0.001))
    }

    @Test
    fun `winning against an equally rated opponent increases the rating by half the K-factor`() {
        // Given
        val currentRating = 2000
        val expected = expectedScore(currentRating, 2000)

        // When
        val updated = newRating(currentRating, expected, actualScore = GameResult.WIN.points, kFactor = 20)

        // Then
        assertThat(updated).isEqualTo(2010)
    }

    @Test
    fun `losing against an equally rated opponent decreases the rating by half the K-factor`() {
        // Given
        val currentRating = 2000
        val expected = expectedScore(currentRating, 2000)

        // When
        val updated = newRating(currentRating, expected, actualScore = GameResult.LOSS.points, kFactor = 20)

        // Then
        assertThat(updated).isEqualTo(1990)
    }

    @Test
    fun `drawing against an equally rated opponent leaves the rating unchanged`() {
        // Given
        val currentRating = 2000
        val expected = expectedScore(currentRating, 2000)

        // When
        val updated = newRating(currentRating, expected, actualScore = GameResult.DRAW.points, kFactor = 20)

        // Then
        assertThat(updated).isEqualTo(2000)
    }

    @Test
    fun `an upset win against a much stronger opponent yields close to the full K-factor gain`() {
        // Given
        val currentRating = 1800
        val expected = expectedScore(currentRating, 2200)

        // When
        val updated = newRating(currentRating, expected, actualScore = GameResult.WIN.points, kFactor = 20)

        // Then
        assertThat(updated).isEqualTo(1818)
    }
}
