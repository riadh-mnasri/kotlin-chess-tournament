# kotlin-chess-tournament

[🇫🇷 Version française](README.md)

[![Build](https://github.com/riadh-mnasri/kotlin-chess-tournament/actions/workflows/ci.yml/badge.svg)](https://github.com/riadh-mnasri/kotlin-chess-tournament/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A Kotlin library for running Swiss-system chess tournaments: pairings, Elo rating calculation, and final standings with tie-breaks (Buchholz, Sonneborn-Berger).

## Why this library

There are good JVM libraries for PGN/FEN parsing and legal move generation (for example [chesslib](https://github.com/bhlangonijr/chesslib) or [kchesslib](https://github.com/cvb941/kchesslib)), but none of them implement Swiss-system pairing or tournament Elo calculation. The only available Swiss pairing engines are written in C++/Pascal ([bbpPairings](https://github.com/BieremaBoyzProgramming/bbpPairings)) or JavaScript. `kotlin-chess-tournament` fills that gap on the JVM/Kotlin side.

## What the library does

- **Swiss-system pairing** (Dutch variant): first round by splitting the ranking into a top and bottom half, later rounds by score group while avoiding repeat pairings.
- **Bye handling**: the bye rotates between players, never falling on the same player twice while another candidate is available.
- **Color allocation**: each player alternates between white and black, avoiding three games in a row with the same color.
- **Elo rating calculation**: expected score, rating update, and a simplified K-factor rule inspired by the FIDE table.
- **Final standings**: score, then Buchholz and Sonneborn-Berger tie-breaks, always in a deterministic order.

## What the library does not do (yet)

This library implements a **pragmatic subset** of the official FIDE Dutch system, not its full specification:

- No strict compliance with the C1 to C20 criteria from the FIDE handbook.
- No accelerated pairings.
- The Elo K-factor is a simplified rule (junior / standard / top player), not the full FIDE table (number of rated games, federation, and so on).
- Tie-breaks are limited to Buchholz and Sonneborn-Berger; other FIDE criteria (average rating of opponents, direct encounter...) are not implemented.

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
    implementation("io.github.riadh-mnasri:kotlin-chess-tournament:0.1.0-SNAPSHOT")
}
```

## Quick start

```kotlin
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.pairing.pairFirstRound
import io.github.riadhmnasri.chesstournament.rating.expectedScore
import io.github.riadhmnasri.chesstournament.rating.newRating
import io.github.riadhmnasri.chesstournament.standings.computeStandings

val alice = Player(id = "1", name = "Alice", rating = 2200)
val bob = Player(id = "2", name = "Bob", rating = 2100)

// Pair the first round
val round1 = pairFirstRound(listOf(alice, bob))

// Compute the Elo change after a white win
val expected = expectedScore(alice.rating, bob.rating)
val aliceNewRating = newRating(alice.rating, expected, actualScore = 1.0, kFactor = 20)

// Standings from the rounds played so far
val standings = computeStandings(listOf(alice, bob), rounds = emptyList())
```

For a full example that plays out several rounds and computes standings and rating changes, see [`examples/src/main/kotlin/.../RunSampleTournament.kt`](examples/src/main/kotlin/io/github/riadhmnasri/chesstournament/examples/RunSampleTournament.kt), runnable with:

```bash
./gradlew :examples:run
```

## The rules, explained simply

**The Swiss system** avoids repeat matchups and brings players of similar strength together as the tournament progresses: in the first round, the field is split into a top and bottom half by rating, and the nth player of the top half plays the nth player of the bottom half. In later rounds, players are grouped by score (same number of points) and paired within that group, avoiding any pairing that has already happened.

**Elo rating** estimates a player's win probability against another from the rating gap between them (a 200 point gap is roughly a 3-in-4 expected result for the stronger player), then adjusts the rating after the game: the more surprising the result, the bigger the adjustment. The "K-factor" controls how large that adjustment can be.

**Buchholz** adds up the final scores of every opponent a player actually faced: the better your opponents finished the tournament, the higher your Buchholz, which rewards a tough schedule. **Sonneborn-Berger** does the same thing but only counts the score of opponents you beat (in full) or drew (halved), not the ones you lost to.

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
