package io.github.riadhmnasri.chesstournament.examples

import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.GameOutcome
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round
import io.github.riadhmnasri.chesstournament.pairing.pairFirstRound
import io.github.riadhmnasri.chesstournament.pairing.pairNextRound
import io.github.riadhmnasri.chesstournament.rating.expectedScore
import io.github.riadhmnasri.chesstournament.rating.kFactorFor
import io.github.riadhmnasri.chesstournament.rating.newRating
import io.github.riadhmnasri.chesstournament.standings.Standing
import io.github.riadhmnasri.chesstournament.standings.computeStandings
import kotlin.random.Random

private const val ROUND_COUNT = 4
private const val DRAW_PROBABILITY = 0.25
private const val RANDOM_SEED = 42L
private const val NAME_COLUMN_WIDTH = 20

private val random = Random(RANDOM_SEED)

/**
 * A runnable simulation of a small Swiss tournament, meant as living
 * documentation: it pairs every round, plays out games, computes
 * standings and reports rating changes, so you can see every part of
 * this library working together on a single example.
 *
 * Run it with `./gradlew :examples:run`.
 */
fun main() {
    val players = samplePlayers()
    val rounds = mutableListOf<Round>()

    repeat(ROUND_COUNT) { roundIndex ->
        val pairingsForRound = if (rounds.isEmpty()) pairFirstRound(players) else pairNextRound(players, rounds)
        val games = pairingsForRound.pairings.map { pairing -> playGame(pairing.white, pairing.black) }
        val round = Round(number = roundIndex + 1, games = games, byePlayer = pairingsForRound.byePlayer)
        rounds += round
        printRound(round)
    }

    printStandings(computeStandings(players, rounds))
    printRatingChanges(players, rounds)
}

private fun samplePlayers(): List<Player> =
    listOf(
        Player(id = "1", name = "Anna Rossi", rating = 2350),
        Player(id = "2", name = "Ben Okafor", rating = 2280),
        Player(id = "3", name = "Chloe Dubois", rating = 2240, age = 16),
        Player(id = "4", name = "Diego Fernandez", rating = 2190),
        Player(id = "5", name = "Emma Larsen", rating = 2150),
        Player(id = "6", name = "Farid Haddad", rating = 2100),
        Player(id = "7", name = "Grace Kim", rating = 2050),
    )

/**
 * Simulates a game between [white] and [black] using this library's own
 * [expectedScore] to weigh the outcome: this is a simplified simulation
 * for the purpose of this example, not a statistically rigorous model.
 */
private fun playGame(
    white: Player,
    black: Player,
): Game {
    val whiteWinProbability = expectedScore(white.rating, black.rating) * (1 - DRAW_PROBABILITY)
    val roll = random.nextDouble()
    val outcome =
        when {
            roll < whiteWinProbability -> GameOutcome.WHITE_WINS
            roll < whiteWinProbability + DRAW_PROBABILITY -> GameOutcome.DRAW
            else -> GameOutcome.BLACK_WINS
        }
    return Game(white, black, outcome)
}

/**
 * Replays [rounds] for [player], applying [newRating] game by game from
 * their starting rating. Byes are excluded, matching standard practice:
 * they count towards the tournament score but not towards rating changes.
 */
private fun finalRatingFor(
    player: Player,
    rounds: List<Round>,
): Int {
    var currentRating = player.rating
    for (round in rounds) {
        val game = round.games.firstOrNull { it.white == player || it.black == player } ?: continue
        val opponent = game.opponentOf(player) ?: continue
        val expected = expectedScore(currentRating, opponent.rating)
        val kFactor = kFactorFor(currentRating, player.age)
        currentRating = newRating(currentRating, expected, game.pointsFor(player), kFactor)
    }
    return currentRating
}

private fun GameOutcome.asScoreLine(): String =
    when (this) {
        GameOutcome.WHITE_WINS -> "1-0"
        GameOutcome.BLACK_WINS -> "0-1"
        GameOutcome.DRAW -> "1/2-1/2"
    }

private fun printRound(round: Round) {
    println("Round ${round.number}")
    round.games.forEach { game ->
        println("  ${game.white.name} (white) vs ${game.black.name} (black): ${game.outcome.asScoreLine()}")
    }
    round.byePlayer?.let { byePlayer -> println("  ${byePlayer.name} has the bye") }
    println()
}

private fun printStandings(standings: List<Standing>) {
    println("Final standings")
    standings.forEachIndexed { index, standing ->
        val name = standing.player.name.padEnd(NAME_COLUMN_WIDTH)
        println(
            "  ${index + 1}. $name score=${standing.score} " +
                "buchholz=${standing.buchholz} sonnebornBerger=${standing.sonnebornBerger} " +
                "aro=${standing.averageRatingOfOpponents}",
        )
    }
    println()
}

private fun printRatingChanges(
    players: List<Player>,
    rounds: List<Round>,
) {
    println("Rating changes")
    players
        .map { player -> player to finalRatingFor(player, rounds) }
        .sortedByDescending { (player, newRating) -> newRating - player.rating }
        .forEach { (player, newRating) ->
            val delta = newRating - player.rating
            val sign = if (delta >= 0) "+" else ""
            println("  ${player.name.padEnd(NAME_COLUMN_WIDTH)} ${player.rating} -> $newRating ($sign$delta)")
        }
}
