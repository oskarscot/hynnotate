dependencies {
    implementation(project(":hynnotate-annotations"))
    implementation("com.palantir.javapoet:javapoet:0.15.0")

    testImplementation("com.google.testing.compile:compile-testing:0.23.0")
    testImplementation(project(":hynnotate-annotations"))
}
