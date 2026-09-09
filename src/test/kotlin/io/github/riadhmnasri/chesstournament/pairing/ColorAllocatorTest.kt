package io.github.riadhmnasri.chesstournament.pairing

import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.GameOutcome
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ColorAllocatorTest {
    private val alice = Player(id = "1", name = "Alice", rating = 2400)
    private val bob = Player(id = "2", name = "Bob", rating = 2300)

    @Test
    fun `the player who has played black more often gets white this time`() {
        // Given: Alice had white and Bob had black in their only game so far
        val previousRounds = listOf(Round(number = 1, games = listOf(Game(alice, bob, GameOutcome.DRAW))))

        // When
        val (white, black) = allocateColors(alice, bob, previousRounds)

        // Then
        assertThat(white).isEqualTo(bob)
        assertThat(black).isEqualTo(alice)
    }

    @Test
    fun `when the color balance is tied, whoever did not have white last round gets white`() {
        // Given: both players have an even color balance (one white, one black game
        // each), but Alice's most recent game was black and Bob's was white
        val someoneElse = Player(id = "3", name = "Someone Else", rating = 2000)
        val anotherPlayer = Player(id = "4", name = "Another Player", rating = 2000)
        val previousRounds =
            listOf(
                Round(
                    number = 1,
                    games =
                        listOf(
                            Game(alice, someoneElse, GameOutcome.DRAW),
                            Game(anotherPlayer, bob, GameOutcome.DRAW),
                        ),
                ),
                Round(
                    number = 2,
                    games =
                        listOf(
                            Game(someoneElse, alice, GameOutcome.DRAW),
                            Game(bob, anotherPlayer, GameOutcome.DRAW),
                        ),
                ),
            )

        // When
        val (white, black) = allocateColors(alice, bob, previousRounds)

        // Then: Bob had white in round 2, so Alice gets it this time
        assertThat(white).isEqualTo(alice)
        assertThat(black).isEqualTo(bob)
    }

    @Test
    fun `when neither player has played yet, the higher rated player gets white`() {
        // Given: no game history at all

        // When
        val (white, black) = allocateColors(alice, bob, previousRounds = emptyList())

        // Then
        assertThat(white).isEqualTo(alice)
        assertThat(black).isEqualTo(bob)
    }

    @Test
    fun `across many rounds of real pairing, no player's color difference ever exceeds plus or minus 2`() {
        // Given: an 8 player field, paired and played for 30 rounds with
        // randomized (but seeded, reproducible) results
        val players = (1..8).map { rank -> Player(id = "$rank", name = "Player $rank", rating = 2500 - rank * 10) }
        val outcomes = listOf(GameOutcome.WHITE_WINS, GameOutcome.BLACK_WINS, GameOutcome.DRAW)
        val random = kotlin.random.Random(7)
        val rounds = mutableListOf<Round>()

        // When: simulate the tournament, checking the invariant after every round
        repeat(30) { roundIndex ->
            val pairings =
                if (rounds.isEmpty()) pairFirstRound(players) else pairNextRound(players, rounds)
            val games =
                pairings.pairings.map { pairing -> Game(pairing.white, pairing.black, outcomes.random(random)) }
            rounds.add(Round(number = roundIndex + 1, games = games, byePlayer = pairings.byePlayer))

            // Then
            players.forEach { player ->
                val balance = rounds.flatMap { it.games }.sumOf { game -> colorContribution(game, player) }
                assertThat(balance).isBetween(-2, 2)
            }
        }
    }

    private fun colorContribution(
        game: Game,
        player: Player,
    ): Int =
        when (player) {
            game.white -> 1
            game.black -> -1
            else -> 0
        }
}
