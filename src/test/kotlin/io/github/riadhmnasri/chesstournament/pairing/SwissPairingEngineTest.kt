package io.github.riadhmnasri.chesstournament.pairing

import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.GameOutcome
import io.github.riadhmnasri.chesstournament.model.Pairing
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round
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

    @Test
    fun `second round groups players by score and avoids repeating round 1 pairings`() {
        // Given: round 1 already played, Alice beat Charlie and Bob beat Dave
        val alice = Player(id = "1", name = "Alice", rating = 2400)
        val bob = Player(id = "2", name = "Bob", rating = 2300)
        val charlie = Player(id = "3", name = "Charlie", rating = 2200)
        val dave = Player(id = "4", name = "Dave", rating = 2100)
        val round1 =
            Round(
                number = 1,
                games =
                    listOf(
                        Game(alice, charlie, GameOutcome.WHITE_WINS),
                        Game(dave, bob, GameOutcome.BLACK_WINS),
                    ),
            )

        // When
        val result = pairNextRound(listOf(alice, bob, charlie, dave), listOf(round1))

        // Then: the two round 1 winners (Alice, Bob) are now paired together,
        // and the two round 1 losers (Charlie, Dave) are paired together.
        // Bob had black in round 1 so he gets white now; Charlie had black
        // in round 1 so he gets white now too.
        assertThat(result.byePlayer).isNull()
        assertThat(result.pairings).containsExactlyInAnyOrder(
            Pairing(white = bob, black = alice),
            Pairing(white = charlie, black = dave),
        )
    }

    @Test
    fun `the bye rotates to a player who has not already had one`() {
        // Given: 3 players, Charlie already had the bye in round 1
        val alice = Player(id = "1", name = "Alice", rating = 2400)
        val bob = Player(id = "2", name = "Bob", rating = 2300)
        val charlie = Player(id = "3", name = "Charlie", rating = 2200)
        val round1 =
            Round(
                number = 1,
                games = listOf(Game(alice, bob, GameOutcome.WHITE_WINS)),
                byePlayer = charlie,
            )

        // When
        val result = pairNextRound(listOf(alice, bob, charlie), listOf(round1))

        // Then: Bob gets the bye this time, and Alice plays Charlie,
        // who gets white since he only has a bye and no color history yet
        assertThat(result.byePlayer).isEqualTo(bob)
        assertThat(result.pairings).containsExactly(Pairing(white = charlie, black = alice))
    }

    @Test
    fun `pairing falls back to a repeat when every remaining player has already been faced`() {
        // Given: a complete round-robin between 4 players across 3 rounds,
        // so every possible pair has already played once
        val alice = Player(id = "1", name = "Alice", rating = 2400)
        val bob = Player(id = "2", name = "Bob", rating = 2300)
        val charlie = Player(id = "3", name = "Charlie", rating = 2200)
        val dave = Player(id = "4", name = "Dave", rating = 2100)
        val rounds =
            listOf(
                Round(
                    number = 1,
                    games = listOf(Game(alice, bob, GameOutcome.DRAW), Game(charlie, dave, GameOutcome.DRAW)),
                ),
                Round(
                    number = 2,
                    games = listOf(Game(alice, charlie, GameOutcome.DRAW), Game(bob, dave, GameOutcome.DRAW)),
                ),
                Round(
                    number = 3,
                    games = listOf(Game(alice, dave, GameOutcome.DRAW), Game(bob, charlie, GameOutcome.DRAW)),
                ),
            )

        // When
        val result = pairNextRound(listOf(alice, bob, charlie, dave), rounds)

        // Then: pairing still succeeds, forced to repeat a past pairing, rather
        // than throwing or leaving a player unpaired
        assertThat(result.byePlayer).isNull()
        assertThat(result.pairings).hasSize(2)
        assertThat(result.pairings.flatMap { listOf(it.white, it.black) })
            .containsExactlyInAnyOrder(alice, bob, charlie, dave)
    }
}
