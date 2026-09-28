package io.github.riadhmnasri.chesstournament.team

import io.github.riadhmnasri.chesstournament.model.Pairing
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.pairing.pairGreedily
import io.github.riadhmnasri.chesstournament.pairing.selectBye

/**
 * Two teams paired to play each other. [first] is the team listed first,
 * which plays white on odd boards (see [boards]).
 */
data class TeamPairing(
    val first: Team,
    val second: Team,
) {
    /**
     * The board-by-board pairings for the given lineups, board 1 first,
     * following the usual team competition convention: the team listed
     * first plays white on boards 1, 3, 5... and black on boards 2, 4,
     * 6... Lineups default to each full roster and must have the same
     * size and only contain players from their own roster.
     */
    fun boards(
        firstLineup: List<Player> = first.players,
        secondLineup: List<Player> = second.players,
    ): List<Pairing> {
        require(firstLineup.size == secondLineup.size) {
            "Both lineups must field the same number of boards, got ${firstLineup.size} and ${secondLineup.size}"
        }
        require(first.players.containsAll(firstLineup)) { "The first lineup contains players outside \"${first.id}\"" }
        require(second.players.containsAll(secondLineup)) {
            "The second lineup contains players outside \"${second.id}\""
        }
        return firstLineup.zip(secondLineup).mapIndexed { index, (firstPlayer, secondPlayer) ->
            if (index % 2 == 0) Pairing(firstPlayer, secondPlayer) else Pairing(secondPlayer, firstPlayer)
        }
    }
}

/** The outcome of pairing one team round: which teams meet, and which one sat out, if any. */
data class TeamRoundPairings(
    val pairings: List<TeamPairing>,
    val byeTeam: Team? = null,
)

/**
 * Pairs teams for the first round of a team Swiss tournament, the same
 * way [io.github.riadhmnasri.chesstournament.pairing.pairFirstRound]
 * pairs players: teams ranked by average roster rating, top half against
 * bottom half, the lowest-rated team getting the bye if their number is
 * odd, and the team listed first alternating from one pairing to the
 * next.
 */
fun pairFirstTeamRound(teams: List<Team>): TeamRoundPairings {
    val ranked = teams.sortedWith(compareByDescending<Team> { it.averageRating() }.thenBy { it.name })
    val byeTeam = ranked.lastOrNull().takeIf { ranked.size % 2 != 0 }
    val pairable = ranked.filterNot { it == byeTeam }

    val halfSize = pairable.size / 2
    val pairings =
        pairable.take(halfSize).zip(pairable.takeLast(halfSize)).mapIndexed { index, (top, bottom) ->
            if (index % 2 == 0) TeamPairing(top, bottom) else TeamPairing(bottom, top)
        }
    return TeamRoundPairings(pairings, byeTeam)
}

/**
 * Pairs teams for a round after the first, given [previousRounds], with
 * the same greedy scheme as
 * [io.github.riadhmnasri.chesstournament.pairing.pairNextRound]: teams
 * ranked by [computeTeamStandings] (under [scoring]), the bye to the
 * lowest-ranked team without a prior one, and each team matched with the
 * highest-ranked remaining team it has not met yet. Two teams that
 * already met are never paired again unless it is unavoidable, even
 * though their individual board pairings would all be new.
 *
 * Who is listed first (and so plays white on odd boards) follows the
 * same rules as individual colors, at team level: the team listed first
 * less often so far, then the team not listed first last round, then the
 * higher-ranked team.
 */
fun pairNextTeamRound(
    teams: List<Team>,
    previousRounds: List<TeamRound>,
    scoring: TeamScoring = TeamScoring(),
): TeamRoundPairings {
    val ranked = computeTeamStandings(teams, previousRounds, scoring).map { it.team }
    val byeTeam = selectBye(ranked) { team -> previousRounds.any { it.byeTeam == team } }

    val pairings =
        pairGreedily(ranked.filterNot { it == byeTeam }) { teamA, teamB ->
            previousRounds.flatMap { it.matches }.any { it.involves(teamA) && it.involves(teamB) }
        }.map { (top, opponent) -> allocateFirstSlot(top, opponent, previousRounds) }
    return TeamRoundPairings(pairings, byeTeam)
}

private fun allocateFirstSlot(
    higherRanked: Team,
    lowerRanked: Team,
    previousRounds: List<TeamRound>,
): TeamPairing {
    val balanceHigher = firstSlotBalance(higherRanked, previousRounds)
    val balanceLower = firstSlotBalance(lowerRanked, previousRounds)
    if (balanceHigher != balanceLower) {
        return if (balanceHigher < balanceLower) {
            TeamPairing(higherRanked, lowerRanked)
        } else {
            TeamPairing(lowerRanked, higherRanked)
        }
    }

    val higherWasFirstLast = wasListedFirstLast(higherRanked, previousRounds)
    val lowerWasFirstLast = wasListedFirstLast(lowerRanked, previousRounds)
    return if (higherWasFirstLast == true && lowerWasFirstLast != true) {
        TeamPairing(lowerRanked, higherRanked)
    } else {
        TeamPairing(higherRanked, lowerRanked)
    }
}

/** Matches listed first minus matches listed second. */
private fun firstSlotBalance(
    team: Team,
    rounds: List<TeamRound>,
): Int {
    val matches = rounds.flatMap { it.matches }
    return matches.count { it.first == team } - matches.count { it.second == team }
}

/** `true` if listed first in its latest match, `false` if second, `null` if it has not played yet. */
private fun wasListedFirstLast(
    team: Team,
    rounds: List<TeamRound>,
): Boolean? =
    rounds
        .asReversed()
        .flatMap { it.matches }
        .firstOrNull { it.involves(team) }
        ?.let { it.first == team }
