import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("net.fabricmc.fabric-loom-remap")
	`maven-publish`
	id("org.jetbrains.kotlin.jvm") version "2.3.21"
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

repositories {
	maven("https://maven.terraformersmc.com/releases/")
	maven("https://maven.shedaniel.me/")
	maven("https://api.modrinth.com/maven")
	mavenCentral()
}

loom {
	mods {
		register("turtmod") {
			sourceSet(sourceSets.main.get())
		}
	}
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
	mappings("net.fabricmc:intermediary:${providers.gradleProperty("minecraft_version").get()}:v2")
	modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")
	implementation(files("src/main/resources/META-INF/jars/DiscordIPC-0.11.3.jar"))

	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
	modImplementation("net.fabricmc:fabric-language-kotlin:${providers.gradleProperty("fabric_kotlin_version").get()}")
	modImplementation("me.shedaniel.cloth:cloth-config-fabric:${providers.gradleProperty("cloth_config_version").get()}")
	modImplementation("com.terraformersmc:modmenu:${providers.gradleProperty("modmenu_version").get()}")
	modImplementation("maven.modrinth:walksylib:${providers.gradleProperty("walksylib_version").get()}")
	modImplementation("maven.modrinth:ukulib:${providers.gradleProperty("ukulib_version").get()}")
}

tasks.processResources {
	val version = version
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 21
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.JVM_21
	}
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
	val projectName = project.name
	inputs.property("projectName", projectName)

	from("LICENSE") {
		rename { "${it}_$projectName" }
	}
}

// configure the maven publication
publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}
