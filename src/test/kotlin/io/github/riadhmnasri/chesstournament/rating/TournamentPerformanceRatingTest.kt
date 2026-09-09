package io.github.riadhmnasri.chesstournament.rating

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

class TournamentPerformanceRatingTest {
    @Test
    fun `scoring exactly 50 percent against a fixed-rating field gives a TPR equal to that field's rating`() {
        // Given: 4 games against 2000-rated opponents, 2 points scored (50 percent)
        val opponentRatings = listOf(2000, 2000, 2000, 2000)
        val actualScore = 2.0

        // When
        val tpr = tournamentPerformanceRating(actualScore, opponentRatings)

        // Then
        assertThat(tpr).isCloseTo(2000, Offset.offset(1))
    }

    @Test
    fun `a perfect score gives a TPR well above the field's average rating`() {
        // Given: 4 games against 2000-rated opponents, a perfect score
        val opponentRatings = listOf(2000, 2000, 2000, 2000)
        val actualScore = 4.0

        // When
        val tpr = tournamentPerformanceRating(actualScore, opponentRatings)

        // Then
        assertThat(tpr).isGreaterThan(2400)
    }

    @Test
    fun `a zero score gives a TPR well below the field's average rating`() {
        // Given: 4 games against 2000-rated opponents, a zero score
        val opponentRatings = listOf(2000, 2000, 2000, 2000)
        val actualScore = 0.0

        // When
        val tpr = tournamentPerformanceRating(actualScore, opponentRatings)

        // Then
        assertThat(tpr).isLessThan(1600)
    }

    @Test
    fun `TPR accounts for the actual rating of each individual opponent, not just the average`() {
        // Given: same 50 percent score (1 win, 1 loss) against a strong and a weak opponent
        val opponentRatings = listOf(2400, 1600)
        val actualScore = 1.0

        // When
        val tpr = tournamentPerformanceRating(actualScore, opponentRatings)

        // Then: beating the weak player and losing to the strong one is a below-average result
        // against this pair's average rating of 2000, since expected score against the weak
        // player alone was already well above 0.5
        assertThat(tpr).isLessThan(2000)
    }
}
