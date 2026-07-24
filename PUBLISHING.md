# Publishing to Maven Central / Publication sur Maven Central

## English

The Gradle build is already fully configured to publish this library to Maven Central through the [Sonatype Central Portal](https://central.sonatype.com), using the [vanniktech maven-publish plugin](https://github.com/vanniktech/gradle-maven-publish-plugin). What is **not** done yet, and needs a human with account access, is the one-time account and key setup below.

### One-time setup

1. **Create a Central Portal account** at https://central.sonatype.com and sign in.
2. **Verify the `io.github.riadh-mnasri` namespace.** Since this project uses the `io.github.<username>` namespace convention, verification is done by signing in with the matching GitHub account through Central Portal (no domain ownership needed).
3. **Generate a GPG key pair** to sign published artifacts:
   ```bash
   gpg --gen-key
   gpg --keyserver keyserver.ubuntu.com --send-keys <your-key-id>
   gpg --export-secret-keys --armor <your-key-id> > private-key.asc
   ```
   Keep `private-key.asc` out of the repository; it is only used to fill in a GitHub secret (next step) and can then be deleted locally.
4. **Generate a Central Portal user token** (Account → Generate User Token on central.sonatype.com). This token, not your account password, is what the build uses to authenticate.
5. **Add four repository secrets** under *Settings → Secrets and variables → Actions* on GitHub:
   - `ORG_GRADLE_PROJECT_mavenCentralUsername`: the user token's username part
   - `ORG_GRADLE_PROJECT_mavenCentralPassword`: the user token's password part
   - `ORG_GRADLE_PROJECT_signingInMemoryKey`: the full contents of `private-key.asc`
   - `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword`: the passphrase you set for the GPG key

### Releasing a version

Once the secrets are in place, bump `version` in `build.gradle.kts` (drop the `-SNAPSHOT` suffix), commit, then push a tag matching `v*`, for example:

```bash
git tag v0.1.0
git push origin v0.1.0
```

This triggers `.github/workflows/release.yml`, which builds and publishes to Maven Central. It can take a few minutes to a few hours for a new release to become visible on Maven Central after publishing.

### Publishing locally without any of this

`./gradlew publishToMavenLocal` works today, with no account or key needed: it publishes to `~/.m2/repository` for local use, and automatically skips signing since no signing credentials are present. This is what the README's installation instructions rely on until the first real release is published.

## Français

Le build Gradle est déjà entièrement configuré pour publier cette librairie sur Maven Central via le [Sonatype Central Portal](https://central.sonatype.com), avec le [plugin vanniktech maven-publish](https://github.com/vanniktech/gradle-maven-publish-plugin). Ce qui **n'est pas** encore fait, et nécessite un accès humain aux comptes, c'est la configuration ponctuelle ci-dessous.

### Configuration à faire une seule fois

1. **Créer un compte Central Portal** sur https://central.sonatype.com et se connecter.
2. **Vérifier le namespace `io.github.riadh-mnasri`.** Ce projet utilise la convention de namespace `io.github.<pseudo>`, donc la vérification se fait simplement en se connectant avec le compte GitHub correspondant via Central Portal (pas besoin de posséder un nom de domaine).
3. **Générer une paire de clés GPG** pour signer les artefacts publiés :
   ```bash
   gpg --gen-key
   gpg --keyserver keyserver.ubuntu.com --send-keys <votre-key-id>
   gpg --export-secret-keys --armor <votre-key-id> > private-key.asc
   ```
   Ne mettez jamais `private-key.asc` dans le repo ; il sert uniquement à remplir un secret GitHub (étape suivante), puis peut être supprimé localement.
4. **Générer un « user token » Central Portal** (Account → Generate User Token sur central.sonatype.com). C'est ce token, et non le mot de passe du compte, que le build utilise pour s'authentifier.
5. **Ajouter quatre secrets de repository** dans *Settings → Secrets and variables → Actions* sur GitHub :
   - `ORG_GRADLE_PROJECT_mavenCentralUsername` : la partie identifiant du user token
   - `ORG_GRADLE_PROJECT_mavenCentralPassword` : la partie mot de passe du user token
   - `ORG_GRADLE_PROJECT_signingInMemoryKey` : le contenu complet de `private-key.asc`
   - `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword` : la passphrase choisie pour la clé GPG

### Publier une version

Une fois les secrets en place, incrémentez `version` dans `build.gradle.kts` (retirez le suffixe `-SNAPSHOT`), committez, puis poussez un tag correspondant à `v*`, par exemple :

```bash
git tag v0.1.0
git push origin v0.1.0
```

Cela déclenche `.github/workflows/release.yml`, qui build et publie sur Maven Central. Il peut s'écouler de quelques minutes à quelques heures avant qu'une nouvelle version soit visible sur Maven Central après publication.

### Publier en local sans rien de tout ça

`./gradlew publishToMavenLocal` fonctionne dès aujourd'hui, sans compte ni clé : cela publie dans `~/.m2/repository` pour un usage local, et la signature est automatiquement ignorée en l'absence d'identifiants de signature. C'est ce sur quoi s'appuient les instructions d'installation du README en attendant la première vraie publication.
