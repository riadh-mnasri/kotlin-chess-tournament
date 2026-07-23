package io.github.riadhmnasri.chesstournament.pairing

import io.github.riadhmnasri.chesstournament.model.Pairing
import io.github.riadhmnasri.chesstournament.model.Player

/** The outcome of pairing one round: who plays whom, and who sat out, if anyone. */
data class RoundPairings(
    val pairings: List<Pairing>,
    val byePlayer: Player? = null,
)
