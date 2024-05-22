rootProject.name = "MinigameCore"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.wagyourtail.xyz/releases")
        maven("https://maven.wagyourtail.xyz/snapshots")
        gradlePluginPortal()
    }
}

val localconfig = file("../UniConfig")
if (localconfig.exists()) {
    include(":uniconfig")
    project(":uniconfig").projectDir = localconfig
}