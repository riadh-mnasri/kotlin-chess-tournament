# kotlin-chess-tournament

[🇫🇷 Version française](README.md)

[![Build](https://github.com/riadh-mnasri/kotlin-chess-tournament/actions/workflows/ci.yml/badge.svg)](https://github.com/riadh-mnasri/kotlin-chess-tournament/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A Kotlin library for running Swiss-system chess tournaments: pairings, Elo rating calculation, and final standings with tie-breaks (Buchholz, Sonneborn-Berger).

## Why this library

There are good JVM libraries for PGN/FEN parsing and legal move generation (for example [chesslib](https://github.com/bhlangonijr/chesslib) or [kchesslib](https://github.com/cvb941/kchesslib)), but none of them implement Swiss-system pairing or tournament Elo calculation. The only available Swiss pairing engines are written in C++/Pascal ([bbpPairings](https://github.com/BieremaBoyzProgramming/bbpPairings)) or JavaScript. `kotlin-chess-tournament` fills that gap on the JVM/Kotlin side.

## What the library does

- **Swiss-system pairing** (Dutch variant): first round by splitting the ranking into a top and bottom half, later rounds by score group while avoiding repeat pairings.
- **Round-robin tournaments**: the full schedule generated at once (circle method), single or double with colors swapped on the second cycle.
- **Bye handling**: the bye rotates between players, never falling on the same player twice while another candidate is available.
- **Color allocation**: each player alternates between white and black, avoiding three games in a row with the same color.
- **Elo rating calculation**: expected score, rating update, and a simplified K-factor rule inspired by the FIDE table.
- **Tournament performance rating (TPR)**: the rating an actual score against the real opponents faced would be worth, computed exactly via binary search (not the traditional "dp lookup table" approximation).
- **Final standings**: score, then Buchholz, Buchholz Cut-1, Sonneborn-Berger, average rating of opponents and direct encounter (within a group that forms a complete mini round-robin) tie-breaks, always in a deterministic order.

## What the library does not do (yet)

This library implements a **pragmatic subset** of the official FIDE Dutch system, not its full specification:

- No strict compliance with the C1 to C20 criteria from the FIDE handbook.
- Accelerated pairings exist but remain optional and opt-in (`pairNextRound(..., virtualPointsByPlayer = ...)`), with two schedules provided as helpers: `acceleratedVirtualPoints` (simplified, fixed top/bottom split) and `bakuAcceleratedVirtualPoints` (FIDE's official Baku algorithm, [FIDE Handbook C.04.7](https://handbook.fide.com/chapter/C0407202602), which depends on group size and round count).
- The Elo K-factor is a simplified rule (junior / standard / top player, including for life once 2400 is reached / new player by rated game count), not the full FIDE table (no variation by federation, and the lifetime flag must be tracked and passed in by the caller — this library has no persistent player history).
- Standings (`computeStandings`) are limited to Buchholz, Buchholz Cut-1, Sonneborn-Berger, average rating of opponents and direct encounter. Number of wins (`numberOfWins`) exists too, but like `buchholzCut1` originally, as a standalone function not yet wired into the sort order; other FIDE criteria are not implemented.

These are good starting points for a first contribution, see [CONTRIBUTING.md](CONTRIBUTING.md).

## Installation

The library is not published on Maven Central yet (in progress, see the roadmap in the issues). In the meantime, you can use it locally:

```bash
git clone https://github.com/riadh-mnasri/kotlin-chess-tournament.git
cd kotlin-chess-tournament
./gradlew publishToMavenLocal
```

Then in your project:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.riadh-mnasri:kotlin-chess-tournament:0.1.0")
}
```

## How to use the library

The library holds no state for you: every function is pure (it takes data in and returns a result, without mutating anything). Your calling code is responsible for keeping the list of players and the history of rounds played so far, and passing it back in on every call.

### 1. Create the players

```kotlin
import io.github.riadhmnasri.chesstournament.model.Player

val players = listOf(
    Player(id = "1", name = "Alice", rating = 2200),
    Player(id = "2", name = "Bob", rating = 2100),
    Player(id = "3", name = "Charlie", rating = 2000),
    Player(id = "4", name = "Dave", rating = 1900),
)
```

`id` must be stable and unique per player: it is how the library recognizes the same player across rounds. `age` is optional and only used by the Elo K-factor rule (see below).

### 2. Pair a round

The first round is paired purely from the players' ratings:

```kotlin
import io.github.riadhmnasri.chesstournament.pairing.pairFirstRound

val roundPairings = pairFirstRound(players)
// roundPairings.pairings   : List<Pairing>, each Pairing has a `white` and a `black`
// roundPairings.byePlayer  : Player?, non-null when the number of players is odd
```

### 3. Play the games and record the round

Once the results are known (entered by a user, generated by a chess engine, simulated...), turn each `Pairing` into a `Game` by attaching a `GameOutcome`, then group them into a `Round`:

```kotlin
import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.GameOutcome
import io.github.riadhmnasri.chesstournament.model.Round

val games = roundPairings.pairings.map { pairing ->
    Game(white = pairing.white, black = pairing.black, outcome = GameOutcome.WHITE_WINS) // replace with the real result
}
val round1 = Round(number = 1, games = games, byePlayer = roundPairings.byePlayer)
```

### 4. Pair the following rounds

From round 2 onward, you need to pass in the history of rounds already played: that is what lets the library avoid repeating a pairing, rotate the bye between players, and balance colors.

```kotlin
import io.github.riadhmnasri.chesstournament.pairing.pairNextRound

val playedRounds = mutableListOf(round1)

val round2Pairings = pairNextRound(players, playedRounds)
// ... play the games as in step 3, build a Round(number = 2, ...)
// then add it to playedRounds before pairing round 3, and so on
```

### 5. Compute the standings

```kotlin
import io.github.riadhmnasri.chesstournament.standings.computeStandings

val standings = computeStandings(players, playedRounds)
standings.forEach { standing ->
    println("${standing.player.name}: ${standing.score} pts (Buchholz ${standing.buchholz}, SB ${standing.sonnebornBerger})")
}
```

`standings` is already sorted from best to worst (see [The rules, explained simply](#the-rules-explained-simply) for the sort order).

### 6. Compute Elo rating changes

A player's result in a `Game` is obtained with `game.pointsFor(player)` (1.0, 0.5, or 0.0), which plugs directly into the Elo calculation:

```kotlin
import io.github.riadhmnasri.chesstournament.rating.expectedScore
import io.github.riadhmnasri.chesstournament.rating.kFactorFor
import io.github.riadhmnasri.chesstournament.rating.newRating

val game = round1.games.first()
val player = game.white
val opponent = game.opponentOf(player)!!

val expected = expectedScore(player.rating, opponent.rating)
val kFactor = kFactorFor(player.rating, player.age)
val playerNewRating = newRating(player.rating, expected, game.pointsFor(player), kFactor)
```

Note that the `Player` objects created in step 1 deliberately keep their starting rating for the whole tournament (that rating is what drives pairing and color balancing): the new ratings computed here should be stored separately, for example in a `Map<Player, Int>`, exactly like the example below does.

### Full example

To see these six steps chained together over a multi-round tournament, printing pairings, standings, and rating changes, see [`examples/src/main/kotlin/.../RunSampleTournament.kt`](examples/src/main/kotlin/io/github/riadhmnasri/chesstournament/examples/RunSampleTournament.kt), runnable with:

```bash
./gradlew :examples:run
```

## The rules, explained simply

**The Swiss system** avoids repeat matchups and brings players of similar strength together as the tournament progresses: in the first round, the field is split into a top and bottom half by rating, and the nth player of the top half plays the nth player of the bottom half. In later rounds, players are grouped by score (same number of points) and paired within that group, avoiding any pairing that has already happened.

**Elo rating** estimates a player's win probability against another from the rating gap between them (a 200 point gap is roughly a 3-in-4 expected result for the stronger player), then adjusts the rating after the game: the more surprising the result, the bigger the adjustment. The "K-factor" controls how large that adjustment can be.

**Buchholz** adds up the final scores of every opponent a player actually faced: the better your opponents finished the tournament, the higher your Buchholz, which rewards a tough schedule. **Buchholz Cut-1** does the same sum, dropping the single lowest-scoring opponent, to reduce the effect of one easy pairing on the tie-break. **Sonneborn-Berger** does the same thing but only counts the score of opponents you beat (in full) or drew (halved), not the ones you lost to. **Average Rating of Opponents** (ARO) simply averages the ratings of every opponent faced: when the three previous criteria are perfectly tied, it breaks the tie in favor of whoever faced the strongest opposition in absolute terms. **Direct encounter** only applies within a group of players still perfectly tied after the four previous criteria, and only when that group forms a complete mini round-robin (every player in it faced every other player exactly once): in that case, the mini-score earned only in those internal games breaks the tie within the group; otherwise (a missing or repeated game, or a cyclic result like A beats B, B beats C, C beats A), the criterion has no effect and standings fall back to alphabetical order.

## Quality and development

```bash
./gradlew build          # compiles, tests, checks style and static analysis
./gradlew test            # tests only
./gradlew ktlintFormat     # auto-formats the code
./gradlew dokkaHtml       # generates the API documentation
```

The project uses [ktlint](https://github.com/pinterest/ktlint) for formatting, [detekt](https://detekt.dev/) for static analysis, [Kover](https://github.com/Kotlin/kotlinx-kover) for test coverage, and [Dokka](https://github.com/Kotlin/dokka) for API documentation. All business logic was written test-first (TDD).

## Publishing

The project is already configured to publish to Maven Central (plugin, POM, signing); only the one-time account and key setup is missing. See [PUBLISHING.md](PUBLISHING.md).

## Contributing

Contributions are welcome, see [CONTRIBUTING.md](CONTRIBUTING.md) for the contribution guide and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for community guidelines. The "What the library does not do (yet)" section above is a good starting point for a first contribution.

## License

[MIT](LICENSE) © 2026 Riadh MNASRI
