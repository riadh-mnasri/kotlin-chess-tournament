# Contribuer

[🇬🇧 English version](CONTRIBUTING.en.md)

Merci de l'intérêt porté à ce projet ! Les contributions sont les bienvenues, qu'il s'agisse d'un rapport de bug, d'une amélioration de la documentation ou d'une nouvelle fonctionnalité.

## Par où commencer

La section « Ce que la librairie ne fait pas (encore) » du [README](README.md) liste des limitations connues et assumées : ce sont de bons points de départ pour une première contribution. Les issues étiquetées [`good first issue`](https://github.com/riadh-mnasri/kotlin-chess-tournament/labels/good%20first%20issue) sont pensées pour être accessibles sans connaître tout le projet.

## Mettre en place le projet en local

```bash
git clone https://github.com/riadh-mnasri/kotlin-chess-tournament.git
cd kotlin-chess-tournament
./gradlew build
```

Ceci compile le projet, exécute les tests, et vérifie le style de code (ktlint) et l'analyse statique (detekt).

## Style de code et TDD

- Le code métier est écrit en TDD : le test s'écrit avant l'implémentation. Un test qui échoue doit être visible avant d'écrire le code qui le fait passer.
- Les tests suivent la structure `// Given` / `// When` / `// Then` en commentaire.
- Le formatage est géré par ktlint : lancez `./gradlew ktlintFormat` avant de committer plutôt que de formater à la main.
- `./gradlew detekt` doit passer sans nouvel avertissement. Si une règle ne convient vraiment pas à un cas précis, discutez-en dans la pull request plutôt que de la désactiver silencieusement.
- Privilégiez des noms explicites et des fonctions courtes à des commentaires expliquant un code compliqué : ce projet vise à rester lisible par quelqu'un qui découvre à la fois Kotlin et les règles du système suisse.

## Proposer une modification

1. Ouvrez une issue avant un gros changement, pour discuter de l'approche.
2. Créez une branche depuis `main`.
3. Committez avec des messages clairs décrivant le *pourquoi* du changement.
4. Vérifiez que `./gradlew build` passe entièrement avant d'ouvrir la pull request.
5. Décrivez dans la pull request ce qui change et pourquoi, en citant l'issue liée le cas échéant.

## Signaler un bug ou proposer une fonctionnalité

Utilisez les templates d'issue GitHub fournis. Plus le rapport est précis (cas d'entrée, comportement attendu, comportement observé), plus vite il pourra être traité.

## Code de conduite

Ce projet suit le [Contributor Covenant](CODE_OF_CONDUCT.md). En participant, vous acceptez de le respecter.
