import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("net.fabricmc.fabric-loom-remap")
	`maven-publish`
	id("org.jetbrains.kotlin.jvm")
}

// The active Minecraft version for this Stonecutter node (e.g. "1.21.11").
val mcVersion: String = stonecutter.current.version

version = "${providers.gradleProperty("mod_version").get()}+$mcVersion"
group = providers.gradleProperty("maven_group").get()
base { archivesName.set("turtmod") }

repositories {
	maven("https://maven.terraformersmc.com/releases/")
	maven("https://maven.shedaniel.me/")
	maven("https://api.modrinth.com/maven")
	mavenCentral()
}

// Per-Minecraft-version dependency coordinates. See PORTING_HANDOFF.md for the full table.
// 1.21.11 mirrors the verified-working baseline exactly; newer nodes use version-matched libs.
data class Deps(
	val fabricApi: String,
	val cloth: String,
	val modmenu: String,
	val kotlin: String,
)

val deps: Deps = when (mcVersion) {
	"1.21.11" -> Deps("0.141.4+1.21.11", "17.0.144", "17.0.0", "1.13.11+kotlin.2.3.21")
	else -> error("No dependency coordinates configured for Minecraft $mcVersion (add a row to build.gradle.kts)")
}

loom {
	mods {
		register("turtmod") {
			sourceSet(sourceSets.main.get())
		}
	}
}

dependencies {
	minecraft("com.mojang:minecraft:$mcVersion")
	mappings("net.fabricmc:intermediary:$mcVersion:v2")
	modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")
	implementation(files(rootProject.file("src/main/resources/META-INF/jars/DiscordIPC-0.11.3.jar")))

	modImplementation("net.fabricmc.fabric-api:fabric-api:${deps.fabricApi}")
	modImplementation("net.fabricmc:fabric-language-kotlin:${deps.kotlin}")
	modImplementation("me.shedaniel.cloth:cloth-config-fabric:${deps.cloth}")
	modImplementation("com.terraformersmc:modmenu:${deps.modmenu}")
}

tasks.processResources {
	val props = mapOf(
		"version" to version,
		"minecraft" to mcVersion,
	)
	props.forEach { (k, v) -> inputs.property(k, v) }

	filesMatching("fabric.mod.json") {
		expand(props)
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
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
	val projectName = project.name
	inputs.property("projectName", projectName)

	from(rootProject.file("LICENSE")) {
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

	repositories {
		// Add repositories to publish to here.
	}
}
