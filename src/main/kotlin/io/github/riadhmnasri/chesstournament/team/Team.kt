package io.github.riadhmnasri.chesstournament.team

import io.github.riadhmnasri.chesstournament.model.Player

/**
 * A team taking part in a team tournament. [players] is the full roster,
 * in board order (board 1 first); a match lineup can be any subset of it,
 * which leaves room for reserves.
 */
data class Team(
    val id: String,
    val name: String,
    val players: List<Player>,
) {
    init {
        require(id.isNotBlank()) { "Team id must not be blank" }
        require(players.isNotEmpty()) { "Team \"$id\" must have at least one player" }
        require(players.map { it.id }.toSet().size == players.size) {
            "Team \"$id\" lists the same player more than once"
        }
    }

    /** The average rating of the whole roster, used to seed the first round and as a last-resort tie-break. */
    fun averageRating(): Double = players.map { it.rating }.average()
}
