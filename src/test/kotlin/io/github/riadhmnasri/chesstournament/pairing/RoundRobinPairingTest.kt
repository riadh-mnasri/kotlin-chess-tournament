package io.github.riadhmnasri.chesstournament.pairing

import io.github.riadhmnasri.chesstournament.model.Player
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test

private fun allUnorderedPairs(players: List<Player>): List<Set<Player>> =
    players.indices.flatMap { i -> (i + 1 until players.size).map { j -> setOf(players[i], players[j]) } }

class RoundRobinPairingTest {
    private val alice = Player(id = "1", name = "Alice", rating = 2400)
    private val bob = Player(id = "2", name = "Bob", rating = 2300)
    private val charlie = Player(id = "3", name = "Charlie", rating = 2200)
    private val dave = Player(id = "4", name = "Dave", rating = 2100)

    @Test
    fun `an even field plays N minus 1 rounds with no byes, and every pair meets exactly once`() {
        // Given
        val players = listOf(alice, bob, charlie, dave)

        // When
        val schedule = pairRoundRobin(players)

        // Then: 4 players -> 3 rounds, 2 games each, no byes
        assertThat(schedule).hasSize(3)
        assertThat(schedule).allSatisfy { round -> assertThat(round.byePlayer).isNull() }
        assertThat(schedule).allSatisfy { round -> assertThat(round.pairings).hasSize(2) }

        val playedPairs = schedule.flatMap { it.pairings }.map { setOf(it.white, it.black) }
        assertThat(playedPairs).containsExactlyInAnyOrderElementsOf(allUnorderedPairs(players))
    }

    @Test
    fun `an odd field plays N rounds, with every player sitting out exactly one`() {
        // Given
        val eve = Player(id = "5", name = "Eve", rating = 2000)
        val players = listOf(alice, bob, charlie, dave, eve)

        // When
        val schedule = pairRoundRobin(players)

        // Then: 5 players -> 5 rounds, 2 games and 1 bye each
        assertThat(schedule).hasSize(5)
        assertThat(schedule).allSatisfy { round -> assertThat(round.pairings).hasSize(2) }

        val byes = schedule.mapNotNull { it.byePlayer }
        assertThat(byes).containsExactlyInAnyOrderElementsOf(players)

        val playedPairs = schedule.flatMap { it.pairings }.map { setOf(it.white, it.black) }
        assertThat(playedPairs).containsExactlyInAnyOrderElementsOf(allUnorderedPairs(players))
    }

    @Test
    fun `every player appears in exactly one pairing or the bye each round`() {
        // Given
        val players = listOf(alice, bob, charlie, dave)

        // When
        val schedule = pairRoundRobin(players)

        // Then
        schedule.forEach { round ->
            val playersInRound = round.pairings.flatMap { listOf(it.white, it.black) } + listOfNotNull(round.byePlayer)
            assertThat(playersInRound).containsExactlyInAnyOrderElementsOf(players)
        }
    }

    @Test
    fun `a double round robin plays every pair twice, with colors swapped the second time`() {
        // Given
        val players = listOf(alice, bob, charlie, dave)

        // When
        val schedule = pairRoundRobin(players, doubleRoundRobin = true)

        // Then: twice the rounds of the single cycle
        assertThat(schedule).hasSize(6)

        val singleCycle = schedule.take(3)
        val secondCycle = schedule.drop(3)
        singleCycle.zip(secondCycle).forEach { (firstMeeting, secondMeeting) ->
            assertThat(firstMeeting.pairings).hasSameSizeAs(secondMeeting.pairings)
            firstMeeting.pairings.forEach { pairing ->
                val samePair = setOf(pairing.white, pairing.black)
                val mirrored = secondMeeting.pairings.single { setOf(it.white, it.black) == samePair }
                assertThat(mirrored.white).isEqualTo(pairing.black)
                assertThat(mirrored.black).isEqualTo(pairing.white)
            }
        }
    }

    @Test
    fun `fewer than two players cannot form a round robin`() {
        // Given / When / Then
        assertThatIllegalArgumentException().isThrownBy { pairRoundRobin(listOf(alice)) }
    }
}
