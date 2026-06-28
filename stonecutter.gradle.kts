plugins {
	id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.11" /* [SC] DO NOT EDIT */

// Build every registered version node's `build` task at once.
// Per-node builds are also available directly, e.g. `gradlew :1.21.10:build`.
tasks.register("chiseledBuild") {
	group = "project"
	dependsOn(stonecutter.tasks.named("build"))
}
