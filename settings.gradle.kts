pluginManagement {
    repositories {
        maven("https://repo.smolder.fr/public/")
        gradlePluginPortal()
    }
}

rootProject.name = "hynnotate"

include("hynnotate-annotations")
include("hynnotate-processor")
include("hynnotate-example")
