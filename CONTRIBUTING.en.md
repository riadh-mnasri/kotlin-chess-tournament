# Contributing

[🇫🇷 Version française](CONTRIBUTING.md)

Thanks for your interest in this project! Contributions are welcome, whether it's a bug report, a documentation improvement, or a new feature.

## Where to start

The "What the library does not do (yet)" section of the [README](README.en.md) lists known, deliberately scoped-out limitations: these are good places to start a first contribution. Issues labeled [`good first issue`](https://github.com/riadh-mnasri/kotlin-chess-tournament/labels/good%20first%20issue) are meant to be approachable without knowing the whole project.

## Setting up the project locally

```bash
git clone https://github.com/riadh-mnasri/kotlin-chess-tournament.git
cd kotlin-chess-tournament
./gradlew build
```

This compiles the project, runs the tests, and checks code style (ktlint) and static analysis (detekt).

## Code style and TDD

- Business logic is written test-first: a failing test should exist before the code that makes it pass.
- Tests follow the `// Given` / `// When` / `// Then` comment structure.
- Formatting is handled by ktlint: run `./gradlew ktlintFormat` before committing rather than formatting by hand.
- `./gradlew detekt` must pass without new warnings. If a rule genuinely does not fit a specific case, discuss it in the pull request rather than silently disabling it.
- Favor clear names and short functions over comments explaining complicated code: this project aims to stay readable by someone discovering both Kotlin and the Swiss system rules at the same time.

## Proposing a change

1. Open an issue before a large change, to discuss the approach.
2. Create a branch from `main`.
3. Write commit messages that explain *why* the change is being made.
4. Make sure `./gradlew build` fully passes before opening the pull request.
5. Describe what changes and why in the pull request, referencing the related issue when relevant.

## Reporting a bug or suggesting a feature

Use the provided GitHub issue templates. The more precise the report (input case, expected behavior, observed behavior), the faster it can be addressed.

## Code of conduct

This project follows the [Contributor Covenant](CODE_OF_CONDUCT.md). By participating, you agree to abide by it.
