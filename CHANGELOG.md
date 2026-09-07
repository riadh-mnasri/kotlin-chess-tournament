# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Accelerated pairings, opt-in via `pairNextRound`'s new `virtualPointsByPlayer` parameter (empty by default, no behavior change for existing callers). A ready-made schedule is provided via `acceleratedVirtualPoints`: the top half of the field gets a +1.0 virtual point bonus in round 1 and +0.5 in round 2, for pairing purposes only.


- `kFactorFor` now accepts an optional `ratedGamesPlayed` count and returns the highest K-factor for a player new to the rating list (fewer than 30 rated games), regardless of rating or age.
- `kFactorFor` now accepts an optional `hasEverReachedTopRating` flag: once set, the player keeps the lowest (10) K-factor even after their current rating drops back below 2400. The caller is responsible for tracking and passing this, since the library has no persistent player history.
- `buchholzCut1`, the Buchholz Cut-1 (median Buchholz) tie-break: the same sum as `buchholz`, with the single lowest-scoring real opponent excluded. Not yet wired into `computeStandings`/`Standing`; call it directly.
- `pairRoundRobin`, generating a round-robin tournament's full schedule at once via the circle method, with a rotating bye for an odd field and an optional `doubleRoundRobin` second cycle with colors swapped.
- Direct encounter tie-break, applied as a fifth criterion after average rating of opponents. Only takes effect within a group of players still fully tied after the first four criteria, and only when that group forms a complete mini round-robin (every pair played each other exactly once); otherwise it has no effect and standings fall back to alphabetical order. Implemented as a per-player mini-score rather than a pairwise comparator, so it cannot violate the comparator contract on a cyclic result (A beat B, B beat C, C beat A).

## [0.1.0] - 2026-09-07

### Added

- Elo rating calculation: expected score, rating update, and a simplified FIDE-inspired K-factor rule.
- Standings computation: cumulative score, Buchholz, Sonneborn-Berger and average-rating-of-opponents tie-breaks, deterministic ranking.
- Swiss-system pairing: first round by top-half-versus-bottom-half, subsequent rounds by score group with repeat-pairing avoidance, rotating bye, and color balancing.
- A runnable sample tournament under `examples/`.
- Bilingual README (French/English), CONTRIBUTING guide, Code of Conduct, and security policy.
