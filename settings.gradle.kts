pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		maven("https://maven.kikugie.dev/releases")
		maven("https://maven.kikugie.dev/snapshots")
		mavenCentral()
		gradlePluginPortal()
	}

	plugins {
		id("net.fabricmc.fabric-loom-remap") version providers.gradleProperty("loom_version")
		id("org.jetbrains.kotlin.jvm") version "2.3.21"
	}
}

plugins {
	id("dev.kikugie.stonecutter") version "0.9.4"
}

stonecutter {
	kotlinController = true
	centralScript = "build.gradle.kts"

	create(rootProject) {
		// One node per Minecraft version (node name == MC version, fabric-only).
		version("1.21.11", "1.21.11")

		vcsVersion = "1.21.11"
	}
}

// Should match your modid
rootProject.name = "turtmod"
