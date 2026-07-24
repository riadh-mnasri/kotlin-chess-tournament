package io.github.riadhmnasri.chesstournament.standings

import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.GameOutcome
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

class StandingsCalculatorTest {
    private val playerA = Player(id = "a", name = "Alice", rating = 2000)
    private val playerB = Player(id = "b", name = "Bob", rating = 1900)

    @Test
    fun `total score accumulates points across rounds`() {
        // Given: Alice beats Bob in round 1, then they draw in round 2
        val rounds =
            listOf(
                Round(number = 1, games = listOf(Game(playerA, playerB, GameOutcome.WHITE_WINS))),
                Round(number = 2, games = listOf(Game(playerB, playerA, GameOutcome.DRAW))),
            )

        // When
        val standings = computeStandings(listOf(playerA, playerB), rounds)

        // Then
        val aliceScore = standings.single { it.player == playerA }.score
        val bobScore = standings.single { it.player == playerB }.score
        assertThat(aliceScore).isCloseTo(1.5, Offset.offset(0.0001))
        assertThat(bobScore).isCloseTo(0.5, Offset.offset(0.0001))
    }

    @Test
    fun `a bye is worth a full point`() {
        // Given: Bob sits out round 1 and gets a bye
        val rounds = listOf(Round(number = 1, games = emptyList(), byePlayer = playerB))

        // When
        val standings = computeStandings(listOf(playerA, playerB), rounds)

        // Then
        val bobScore = standings.single { it.player == playerB }.score
        assertThat(bobScore).isCloseTo(1.0, Offset.offset(0.0001))
    }

    @Test
    fun `players are ranked by score, then buchholz, then sonneborn berger`() {
        // Given: a 3 player, 2 round tournament where Alice and Charlie end up
        // tied on score, but Alice faced tougher opposition (higher buchholz)
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
        val standings = computeStandings(listOf(alice, bob, charlie), rounds)

        // Then: Alice (1.5 pts, buchholz 2.5) ranks above Charlie (1.5 pts, buchholz 1.5),
        // who ranks above Bob (1.0 pt)
        assertThat(standings.map { it.player }).containsExactly(alice, charlie, bob)
    }

    @Test
    fun `ties that survive every tie-break are broken alphabetically by name for a deterministic order`() {
        // Given: two players who never play each other or anyone else, so
        // their score, buchholz and sonneborn berger are all zero
        val zoe = Player(id = "z", name = "Zoe", rating = 2000)
        val amir = Player(id = "y", name = "Amir", rating = 2000)

        // When
        val standings = computeStandings(listOf(zoe, amir), rounds = emptyList())

        // Then
        assertThat(standings.map { it.player }).containsExactly(amir, zoe)
    }

    @Test
    fun `average rating of opponents breaks a tie that score, buchholz and sonneborn berger could not`() {
        // Given: Alice and Bob both win their only game, and their opponents
        // both lose their only game, so score, buchholz (both 0, from a
        // losing opponent) and sonneborn berger (both 0, from a 0 score
        // opponent) are all tied on both sides. Alice's opponent is rated
        // higher than Bob's, which only average rating of opponents can see.
        val alice = Player(id = "a", name = "Alice", rating = 2400)
        val bob = Player(id = "b", name = "Bob", rating = 2300)
        val strongOpponent = Player(id = "s", name = "StrongOpponent", rating = 2200)
        val weakOpponent = Player(id = "w", name = "WeakOpponent", rating = 1600)
        val round1 =
            Round(
                number = 1,
                games =
                    listOf(
                        Game(alice, strongOpponent, GameOutcome.WHITE_WINS),
                        Game(bob, weakOpponent, GameOutcome.WHITE_WINS),
                    ),
            )

        // When
        val standings = computeStandings(listOf(alice, bob, strongOpponent, weakOpponent), listOf(round1))

        // Then: Alice ranks above Bob (tougher opponent), and StrongOpponent
        // ranks above WeakOpponent for the same reason, among the losers
        assertThat(standings.map { it.player }).containsExactly(alice, bob, strongOpponent, weakOpponent)
    }
}
