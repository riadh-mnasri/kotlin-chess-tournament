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
- **Classement final** : score, puis départage Buchholz, Sonneborn-Berger et rating moyen des adversaires, avec un ordre toujours déterministe.

## Ce que la librairie ne fait pas (encore)

Cette librairie assume un **sous-ensemble pragmatique** du système Dutch officiel de la FIDE, pas sa spécification intégrale :

- Pas de conformité stricte aux critères C1 à C20 du manuel FIDE.
- Pas d'appariements accélérés (« accelerated pairings »).
- Le K-factor Elo est une règle simplifiée (junior / standard / haut niveau), pas la table FIDE complète (nombre de parties jouées, fédération, etc.).
- Le départage se limite à Buchholz, Sonneborn-Berger et rating moyen des adversaires ; d'autres critères FIDE (confrontation directe...) ne sont pas implémentés.

Ce sont de bons points de départ pour une première contribution : voir [CONTRIBUTING.md](CONTRIBUTING.md).

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

## Comment utiliser la librairie

La librairie ne gère aucun état pour vous : toutes les fonctions sont pures (elles prennent des données en entrée et renvoient un résultat, sans rien modifier). C'est à votre code appelant de conserver la liste des joueurs et l'historique des rounds déjà joués, et de le repasser à chaque appel.

### 1. Créer les joueurs

```kotlin
import io.github.riadhmnasri.chesstournament.model.Player

val players = listOf(
    Player(id = "1", name = "Alice", rating = 2200),
    Player(id = "2", name = "Bob", rating = 2100),
    Player(id = "3", name = "Charlie", rating = 2000),
    Player(id = "4", name = "Dave", rating = 1900),
)
```

`id` doit être stable et unique par joueur : c'est ce qui permet à la librairie de reconnaître un même joueur d'un round à l'autre. `age` est optionnel, il n'est utilisé que par la règle de K-factor Elo (voir plus bas).

### 2. Apparier un round

Le premier round s'apparie uniquement à partir du classement des joueurs :

```kotlin
import io.github.riadhmnasri.chesstournament.pairing.pairFirstRound

val roundPairings = pairFirstRound(players)
// roundPairings.pairings   : List<Pairing>, chaque Pairing a un `white` et un `black`
// roundPairings.byePlayer  : Player?, non nul si le nombre de joueurs est impair
```

### 3. Jouer les parties et enregistrer le round

Une fois les résultats connus (saisis par un utilisateur, générés par un moteur d'échecs, simulés...), transformez chaque `Pairing` en `Game` en lui associant un `GameOutcome`, puis regroupez-les dans un `Round` :

```kotlin
import io.github.riadhmnasri.chesstournament.model.Game
import io.github.riadhmnasri.chesstournament.model.GameOutcome
import io.github.riadhmnasri.chesstournament.model.Round

val games = roundPairings.pairings.map { pairing ->
    Game(white = pairing.white, black = pairing.black, outcome = GameOutcome.WHITE_WINS) // remplacez par le vrai résultat
}
val round1 = Round(number = 1, games = games, byePlayer = roundPairings.byePlayer)
```

### 4. Apparier les rounds suivants

À partir du round 2, il faut passer l'historique des rounds déjà joués : c'est ce qui permet à la librairie d'éviter de refaire une paire déjà jouée, de faire tourner le bye entre les joueurs, et d'équilibrer les couleurs.

```kotlin
import io.github.riadhmnasri.chesstournament.pairing.pairNextRound

val playedRounds = mutableListOf(round1)

val round2Pairings = pairNextRound(players, playedRounds)
// ... jouez les parties comme à l'étape 3, construisez un Round(number = 2, ...)
// puis ajoutez-le à playedRounds avant d'apparier le round 3, et ainsi de suite
```

### 5. Calculer le classement

```kotlin
import io.github.riadhmnasri.chesstournament.standings.computeStandings

val standings = computeStandings(players, playedRounds)
standings.forEach { standing ->
    println("${standing.player.name}: ${standing.score} pts (Buchholz ${standing.buchholz}, SB ${standing.sonnebornBerger})")
}
```

`standings` est déjà trié du meilleur au moins bon (voir [Les règles expliquées simplement](#les-règles-expliquées-simplement) pour l'ordre de tri).

### 6. Calculer l'évolution du rating Elo

Le résultat d'un joueur dans un `Game` s'obtient avec `game.pointsFor(player)` (1.0, 0.5 ou 0.0), qui se branche directement sur le calcul Elo :

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

Notez que les objets `Player` créés à l'étape 1 gardent volontairement leur rating de départ pendant tout le tournoi (c'est ce rating qui sert à l'appariement et à l'équilibrage des couleurs) : les nouveaux ratings calculés ici sont à stocker séparément, par exemple dans une `Map<Player, Int>`, exactement comme le fait l'exemple ci-dessous.

### Exemple complet

Pour voir ces six étapes enchaînées sur un tournoi de plusieurs rounds, avec l'affichage des appariements, du classement et des changements de rating, voir [`examples/src/main/kotlin/.../RunSampleTournament.kt`](examples/src/main/kotlin/io/github/riadhmnasri/chesstournament/examples/RunSampleTournament.kt), exécutable avec :

```bash
./gradlew :examples:run
```

## Les règles expliquées simplement

**Le système suisse** évite de faire jouer les mêmes adversaires plusieurs fois et rapproche les joueurs de niveau proche au fil du tournoi : au premier tour, on coupe le classement en deux moitiés et on fait jouer le n-ième joueur de la moitié haute contre le n-ième de la moitié basse. Aux tours suivants, on regroupe les joueurs par score (ceux qui ont le même nombre de points) et on les apparie entre eux, en évitant de refaire une paire déjà jouée.

**Le rating Elo** estime la probabilité de gain d'un joueur face à un autre à partir de l'écart de rating (200 points d'écart ≈ 3 victoires sur 4 pour le mieux classé), puis ajuste le rating après la partie : plus le résultat est surprenant, plus l'ajustement est important. Le "K-factor" contrôle l'amplitude de cet ajustement.

**Buchholz** additionne les scores finaux de tous les adversaires qu'un joueur a réellement affrontés : plus vos adversaires ont bien terminé le tournoi, plus votre Buchholz est élevé, ce qui récompense un parcours face à une opposition relevée. **Sonneborn-Berger** fait la même chose mais ne compte que les scores des adversaires battus (en entier) ou avec qui on a fait nulle (à moitié), pas ceux contre qui on a perdu. **Le rating moyen des adversaires** (ARO) fait simplement la moyenne des ratings de tous les adversaires affrontés : à égalité parfaite sur les trois critères précédents, il départage en faveur de qui a affronté l'opposition la plus forte en valeur absolue.

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
