package io.github.riadhmnasri.chesstournament.model

/**
 * A tournament participant.
 *
 * [age] is optional and only used by the default K-factor rule in the
 * `rating` package; it can be omitted for adult-only tournaments.
 */
data class Player(
    val id: String,
    val name: String,
    val rating: Int,
    val age: Int? = null,
)
