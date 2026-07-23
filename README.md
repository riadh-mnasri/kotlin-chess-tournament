# kotlin-chess-tournament

[🇬🇧 English version](README.en.md)

[![Build](https://github.com/riadh-mnasri/kotlin-chess-tournament/actions/workflows/ci.yml/badge.svg)](https://github.com/riadh-mnasri/kotlin-chess-tournament/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Une librairie Kotlin pour organiser des tournois d'échecs au système suisse : appariements, calcul de classement Elo, et classement final avec départage (Buchholz, Sonneborn-Berger).

## Pourquoi cette librairie

Il existe de bonnes librairies JVM pour le parsing PGN/FEN et la génération de coups légaux (par exemple [chesslib](https://github.com/bhlangonijr/chesslib) ou [kchesslib](https://github.com/cvb941/kchesslib)), mais aucune n'implémente l'appariement au système suisse ni le calcul de rating Elo pour un tournoi. Les seules implémentations d'appariement suisse disponibles sont en C++/Pascal ([bbpPairings](https://github.com/BieremaBoyzProgramming/bbpPairings)) ou en JavaScript. `kotlin-chess-tournament` comble ce vide côté JVM/Kotlin.

## Ce que fait la librairie

- **Appariement au système suisse** (variante Dutch) : premier tour par répartition haut/bas du classement, tours suivants par groupes de score avec évitement des répétitions.
- **Gestion du bye** : le bye tourne entre les joueurs, il ne tombe jamais deux fois sur le même joueur tant qu'un autre candidat existe.
- **Équilibrage des couleurs** : chaque joueur reçoit alternativement blancs et noirs, en évitant les séries de trois couleurs identiques.
- **Calcul de rating Elo** : score espéré, mise à jour du rating, et une règle de K-factor simplifiée inspirée de la table FIDE.
- **Classement final** : score, puis départage Buchholz et Sonneborn-Berger, avec un ordre toujours déterministe.

## Ce que la librairie ne fait pas (encore)

Cette librairie assume un **sous-ensemble pragmatique** du système Dutch officiel de la FIDE, pas sa spécification intégrale :

- Pas de conformité stricte aux critères C1 à C20 du manuel FIDE.
- Pas d'appariements accélérés (« accelerated pairings »).
- Le K-factor Elo est une règle simplifiée (junior / standard / haut niveau), pas la table FIDE complète (nombre de parties jouées, fédération, etc.).
- Le départage se limite à Buchholz et Sonneborn-Berger ; d'autres critères FIDE (rating moyen des adversaires, confrontation directe...) ne sont pas implémentés.

Ce sont de bons points de départ pour une première contribution — voir [CONTRIBUTING.md](CONTRIBUTING.md).

## Installation

La librairie n'est pas encore publiée sur Maven Central (en cours de préparation, voir la roadmap dans les issues). En attendant, vous pouvez l'utiliser en local :

```bash
git clone https://github.com/riadh-mnasri/kotlin-chess-tournament.git
cd kotlin-chess-tournament
./gradlew publishToMavenLocal
```

Puis dans votre projet :

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.riadh-mnasri:kotlin-chess-tournament:0.1.0-SNAPSHOT")
}
```

## Utilisation rapide

```kotlin
import io.github.riadhmnasri.chesstournament.model.Player
import io.github.riadhmnasri.chesstournament.pairing.pairFirstRound
import io.github.riadhmnasri.chesstournament.rating.expectedScore
import io.github.riadhmnasri.chesstournament.rating.newRating
import io.github.riadhmnasri.chesstournament.standings.computeStandings

val alice = Player(id = "1", name = "Alice", rating = 2200)
val bob = Player(id = "2", name = "Bob", rating = 2100)

// Appariement du premier tour
val round1 = pairFirstRound(listOf(alice, bob))

// Calcul Elo après une victoire des blancs
val expected = expectedScore(alice.rating, bob.rating)
val aliceNewRating = newRating(alice.rating, expected, actualScore = 1.0, kFactor = 20)

// Classement à partir des rounds joués
val standings = computeStandings(listOf(alice, bob), rounds = emptyList())
```

Pour un exemple complet qui enchaîne plusieurs tours, calcule les classements et les nouveaux ratings, voir [`examples/src/main/kotlin/.../RunSampleTournament.kt`](examples/src/main/kotlin/io/github/riadhmnasri/chesstournament/examples/RunSampleTournament.kt), exécutable avec :

```bash
./gradlew :examples:run
```

## Les règles expliquées simplement

**Le système suisse** évite de faire jouer les mêmes adversaires plusieurs fois et rapproche les joueurs de niveau proche au fil du tournoi : au premier tour, on coupe le classement en deux moitiés et on fait jouer le n-ième joueur de la moitié haute contre le n-ième de la moitié basse. Aux tours suivants, on regroupe les joueurs par score (ceux qui ont le même nombre de points) et on les apparie entre eux, en évitant de refaire une paire déjà jouée.

**Le rating Elo** estime la probabilité de gain d'un joueur face à un autre à partir de l'écart de rating (200 points d'écart ≈ 3 victoires sur 4 pour le mieux classé), puis ajuste le rating après la partie : plus le résultat est surprenant, plus l'ajustement est important. Le "K-factor" contrôle l'amplitude de cet ajustement.

**Buchholz** additionne les scores finaux de tous les adversaires qu'un joueur a réellement affrontés : plus vos adversaires ont bien terminé le tournoi, plus votre Buchholz est élevé, ce qui récompense un parcours face à une opposition relevée. **Sonneborn-Berger** fait la même chose mais ne compte que les scores des adversaires battus (en entier) ou avec qui on a fait nulle (à moitié) — pas ceux contre qui on a perdu.

## Qualité et développement

```bash
./gradlew build          # compile, teste, vérifie le style et l'analyse statique
./gradlew test            # tests uniquement
./gradlew ktlintFormat     # reformate automatiquement le code
./gradlew dokkaHtml       # génère la documentation API
```

Le projet utilise [ktlint](https://github.com/pinterest/ktlint) pour le formatage, [detekt](https://detekt.dev/) pour l'analyse statique, [Kover](https://github.com/Kotlin/kotlinx-kover) pour la couverture de tests, et [Dokka](https://github.com/Kotlin/dokka) pour la documentation API. Tout le code métier a été écrit en TDD (tests d'abord).

## Publication

Le projet est déjà configuré pour publier sur Maven Central (plugin, POM, signature), il ne manque que la configuration ponctuelle des comptes et clés. Voir [PUBLISHING.md](PUBLISHING.md).

## Contribuer

Les contributions sont bienvenues, voir [CONTRIBUTING.md](CONTRIBUTING.md) pour le guide de contribution et [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) pour les règles de la communauté. La section « Ce que la librairie ne fait pas (encore) » ci-dessus est un bon point de départ pour une première contribution.

## Licence

[MIT](LICENSE) © 2026 Riadh MNASRI
