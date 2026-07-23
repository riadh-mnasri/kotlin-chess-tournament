plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    application
}

dependencies {
    implementation(project(":"))
}

application {
    mainClass.set("io.github.riadhmnasri.chesstournament.examples.RunSampleTournamentKt")
}

kotlin {
    jvmToolchain(17)
}
