import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
}

group = "io.github.riadh-mnasri"
version = "0.1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(group.toString(), "kotlin-chess-tournament", version.toString())

    pom {
        name.set("kotlin-chess-tournament")
        description.set("Swiss-system pairing, Elo rating and standings for chess tournaments, for Kotlin/JVM.")
        inceptionYear.set("2026")
        url.set("https://github.com/riadh-mnasri/kotlin-chess-tournament")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("riadh-mnasri")
                name.set("Riadh MNASRI")
                url.set("https://github.com/riadh-mnasri")
            }
        }

        scm {
            url.set("https://github.com/riadh-mnasri/kotlin-chess-tournament")
            connection.set("scm:git:git://github.com/riadh-mnasri/kotlin-chess-tournament.git")
            developerConnection.set("scm:git:ssh://git@github.com/riadh-mnasri/kotlin-chess-tournament.git")
        }
    }
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

detekt {
    config.setFrom(files("$rootDir/detekt.yml"))
    buildUponDefaultConfig = true
}
