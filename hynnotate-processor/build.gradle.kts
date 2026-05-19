repositories {
    maven("https://maven.hytale.com/pre-release")
}

dependencies {
    implementation(project(":hynnotate-annotations"))
    implementation("com.palantir.javapoet:javapoet:0.15.0")

    testImplementation("com.google.testing.compile:compile-testing:0.23.0")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("com.hypixel.hytale:Server:2026.05.07-5efa15f6d")
    testImplementation(project(":hynnotate-annotations"))
}
