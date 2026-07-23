package io.github.riadhmnasri.chesstournament.model

/** Two players paired to play each other, with colors already assigned, before any result is known. */
data class Pairing(
    val white: Player,
    val black: Player,
)
