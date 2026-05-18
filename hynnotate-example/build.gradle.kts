import fr.smolder.hytale.gradle.Patchline

plugins {
    id("java")
    id("fr.smolder.hytale.dev") version "0.1.0"
}

repositories {
    maven("https://maven.hytale.com/pre-release")
}

dependencies {
    compileOnly("com.hypixel.hytale:Server:2026.05.07-5efa15f6d")

    compileOnly(project(":hynnotate-annotations"))
    annotationProcessor(project(":hynnotate-processor"))
}

hytale {
    manifest {
        group = "onyxium.dev"
        name = "HynnotateExample"
        version = project.version.toString()
        description = "Hynnotate annotation processor example plugin"
        author("oskarscot")

        serverVersion = "2026.05.07-5efa15f6d"

        main = "dev.onyxium.hynnotate.example.HynnotateExample"

        includesAssetPack = false
    }

    patchLine = Patchline.PRE_RELEASE

    hytalePath.set("/home/oskar/.var/app/com.hypixel.HytaleLauncher/data/Hytale")

    minMemory.set("2G")
    maxMemory.set("4G")

    vineflowerVersion.set("1.11.2")
    decompileFilter.set(listOf("com/hypixel/**"))
    decompilerHeapSize.set("6G")
}
