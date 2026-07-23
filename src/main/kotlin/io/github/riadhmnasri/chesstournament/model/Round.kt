package io.github.riadhmnasri.chesstournament.model

/**
 * One round of a tournament: a set of games, plus the player who sat out
 * this round, if any (`null` when the number of players is even and
 * everyone is paired).
 */
data class Round(
    val number: Int,
    val games: List<Game>,
    val byePlayer: Player? = null,
)
