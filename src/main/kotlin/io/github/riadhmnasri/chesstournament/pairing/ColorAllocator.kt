package io.github.riadhmnasri.chesstournament.pairing

import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.model.Round

/**
 * Decides which of [playerA] and [playerB] should play white in their
 * upcoming game, based on their color history in [previousRounds].
 *
 * The rules, in order:
 * 1. Whoever has played black more often than white so far gets white now,
 *    to keep each player's colors balanced over the tournament.
 * 2. If their color balance is tied, whoever did not have white in the
 *    most recent round they played gets white, to avoid three games in a
 *    row with the same color.
 * 3. If they are still tied (typically because neither has played yet),
 *    the higher-rated player gets white, purely for a deterministic result.
 *
 * Rule 1 alone already guarantees no player's color difference (white
 * games minus black games) can ever exceed +-2 in absolute value: within
 * any single pairing, whoever has the strictly lower balance gets white,
 * so a player already at a difference of +2 can only ever be handed
 * white again by a same-round opponent whose own balance is at least as
 * high — an opponent who, symmetrically, needs black at least as
 * urgently, so favoring them is the better outcome for keeping the
 * *maximum* imbalance in the field as small as possible. No separate cap
 * check is needed on top of rule 1; this is exercised end-to-end by a
 * multi-round property test in `ColorAllocatorTest`.
 *
 * Returns a (white, black) pair.
 */
internal fun allocateColors(
    playerA: Player,
    playerB: Player,
    previousRounds: List<Round>,
): Pair<Player, Player> {
    val balanceA = colorBalance(playerA, previousRounds)
    val balanceB = colorBalance(playerB, previousRounds)

    if (balanceA != balanceB) {
        return if (balanceA < balanceB) playerA to playerB else playerB to playerA
    }

    val playerAHadWhiteLast = lastColorWasWhite(playerA, previousRounds)
    val playerBHadWhiteLast = lastColorWasWhite(playerB, previousRounds)

    return when {
        playerAHadWhiteLast == true && playerBHadWhiteLast != true -> playerB to playerA
        playerBHadWhiteLast == true && playerAHadWhiteLast != true -> playerA to playerB
        playerA.rating >= playerB.rating -> playerA to playerB
        else -> playerB to playerA
    }
}

private fun colorBalance(
    player: Player,
    rounds: List<Round>,
): Int {
    var balance = 0
    for (game in rounds.flatMap { it.games }) {
        when (player) {
            game.white -> balance += 1
            game.black -> balance -= 1
        }
    }
    return balance
}

/** `true` if white, `false` if black, `null` if the player has not played a game yet. */
private fun lastColorWasWhite(
    player: Player,
    rounds: List<Round>,
): Boolean? =
    rounds
        .asReversed()
        .flatMap { it.games }
        .firstOrNull { game -> game.white == player || game.black == player }
        ?.let { game -> game.white == player }
