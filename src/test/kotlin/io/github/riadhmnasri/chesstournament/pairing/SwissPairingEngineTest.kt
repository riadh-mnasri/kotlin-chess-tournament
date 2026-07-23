package io.github.riadhmnasri.chesstournament.pairing

import io.github.riadhmnasri.chesstournament.model.Pairing
import io.github.riadhmnasri.chesstournament.model.Player
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SwissPairingEngineTest {
    @Test
    fun `first round pairs the top half of the field against the bottom half`() {
        // Given: four players ranked 1st to 4th by rating
        val first = Player(id = "1", name = "First", rating = 2400)
        val second = Player(id = "2", name = "Second", rating = 2300)
        val third = Player(id = "3", name = "Third", rating = 2200)
        val fourth = Player(id = "4", name = "Fourth", rating = 2100)

        // When
        val result = pairFirstRound(listOf(first, second, third, fourth))

        // Then: the top half (1st, 2nd) each face the corresponding player
        // of the bottom half (3rd, 4th), and colors alternate between pairings
        assertThat(result.byePlayer).isNull()
        assertThat(result.pairings).containsExactly(
            Pairing(white = first, black = third),
            Pairing(white = fourth, black = second),
        )
    }

    @Test
    fun `every player appears in exactly one pairing`() {
        // Given: six players with distinct ratings
        val players = (1..6).map { rank -> Player(id = "$rank", name = "Player $rank", rating = 2500 - rank * 50) }

        // When
        val result = pairFirstRound(players)

        // Then
        val playersInPairings = result.pairings.flatMap { listOf(it.white, it.black) }
        assertThat(playersInPairings).containsExactlyInAnyOrderElementsOf(players)
    }

    @Test
    fun `an odd number of players sends the lowest rated player to the bye`() {
        // Given: five players, the fifth being the lowest rated
        val players = (1..5).map { rank -> Player(id = "$rank", name = "Player $rank", rating = 2500 - rank * 50) }
        val lowestRated = players.last()

        // When
        val result = pairFirstRound(players)

        // Then
        assertThat(result.byePlayer).isEqualTo(lowestRated)
        assertThat(result.pairings).hasSize(2)
        assertThat(result.pairings.flatMap { listOf(it.white, it.black) })
            .containsExactlyInAnyOrderElementsOf(players - lowestRated)
    }

    @Test
    fun `a single player receives a bye and no pairings are produced`() {
        // Given
        val soloPlayer = Player(id = "1", name = "Solo", rating = 2000)

        // When
        val result = pairFirstRound(listOf(soloPlayer))

        // Then
        assertThat(result.byePlayer).isEqualTo(soloPlayer)
        assertThat(result.pairings).isEmpty()
    }
}
