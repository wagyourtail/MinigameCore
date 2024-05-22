import xyz.wagyourtail.unimined.api.unimined

plugins {
    id("xyz.wagyourtail.unimined") version "1.2.5-SNAPSHOT"
    `maven-publish`
}

base {
    archivesName = project.properties["archives_base_name"] as String
    group = project.properties["maven_group"] as String
    version = project.properties["version"] as String
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val enabledPlatforms: List<String> = project.properties["enabled_platforms"]?.toString()?.split(",") ?: emptyList()

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
}

unimined.minecraft {
    version(project.properties["minecraft_version"] as String)

    mappings {
        mojmap()

        devFallbackNamespace("official")
    }

    accessWidener {

    }

    defaultRemapJar = false
}

val fabricApiVersion = project.properties["fabric_api_version"] as String
val localconfig = file("../UniConfig")
project.evaluationDependsOn(":uniconfig")

if (enabledPlatforms.contains("fabric")) {
    val fabric by sourceSets.creating

    unimined.minecraft(fabric) {
        combineWith(sourceSets.main.get())
        defaultRemapJar = true

        fabric {
            loader(project.properties["fabric_loader_version"] as String)
        }
    }

    dependencies {
        "fabricModImplementation"("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
        if (localconfig.exists()) {
            "fabricImplementation"(project.project(":uniconfig").sourceSets.getByName("fabric").runtimeClasspath)
        } else {
            TODO()
        }
    }

    tasks.named<ProcessResources>("processFabricResources") {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }
}

if (enabledPlatforms.contains("neoforge")) {
    val neoforge by sourceSets.creating

    unimined.minecraft(neoforge) {
        combineWith(sourceSets.main.get())
        defaultRemapJar = true

        neoForged {
            loader(project.properties["neoforge_version"] as String)
        }
    }

    dependencies {
        if (localconfig.exists()) {
            "neoforgeImplementation"(project.project(":uniconfig").sourceSets.getByName("neoforge").runtimeClasspath)
        } else {
            TODO()
        }
    }

    tasks.named<ProcessResources>("processNeoforgeResources") {
        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
            expand("version" to project.version)
        }
    }
}

dependencies {
    compileOnly("org.spongepowered:mixin:0.8.5")
    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
    compileOnly("org.ow2.asm:asm:9.5")
    compileOnly("com.demonwav.mcdev:annotations:2.1.0")

    if (localconfig.exists()) {
        implementation(project.project(":uniconfig").sourceSets.getByName("main").runtimeClasspath)
    } else {
        TODO()
    }
}

java {
    withSourcesJar()
}

tasks.compileJava {
    options.compilerArgs.add("-XDdebug.dumpInferenceGraphsTo=${project.buildDir}/inferenceGraphs")
    file("${project.buildDir}/inferenceGraphs").mkdirs()
}

publishing {
    repositories {
        maven {
            name = "WagYourMaven"
            url = if (project.hasProperty("version_snapshot")) {
                uri("https://maven.wagyourtail.xyz/snapshots/")
            } else {
                uri("https://maven.wagyourtail.xyz/releases/")
            }
            credentials {
                username = project.findProperty("mvn.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("mvn.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group as String
            artifactId = project.properties["archives_base_name"] as String? ?: project.name
            version = project.version as String

            artifact(tasks["jar"]) {
                classifier = null
            }

            artifact(tasks["sourcesJar"]) {
                classifier = "sources"
            }

            if (enabledPlatforms.contains("neoforge")) {
                artifact(tasks["remapNeoforgeJar"]) {
                    classifier = "neoforge"
                }
            }

            if (enabledPlatforms.contains("fabric")) {
                artifact(tasks["remapFabricJar"]) {
                    classifier = "fabric"
                }
            }
        }
    }
}
