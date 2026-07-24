package io.github.riadhmnasri.chesstournament.standings

import io.github.riadhmnasri.chesstournament.model.Player

/** A player's final ranking data after a tournament, or after any number of played rounds. */
data class Standing(
    val player: Player,
    val score: Double,
    val buchholz: Double,
    val sonnebornBerger: Double,
    val averageRatingOfOpponents: Double,
)
