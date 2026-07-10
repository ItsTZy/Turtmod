# TurtMod

A client-side **Fabric** quality-of-life mod focused on **visual clarity, combat feedback, and HUD
customization** — with a custom launcher-style config UI, a movable HUD editor, cosmetics, a screenshot
gallery, command-key macros, and a kit loader. Client-only; no server-side component.

## Supported versions (one branch per version)

The game became unobfuscated at 26.1, which needs a no-remap Loom incompatible with the intermediary
setup used for 1.21.11 — so each supported version lives on its own branch:

| Branch | Minecraft | Mappings | Build |
|---|---|---|---|
| `master` | 26.2 | Mojang (unobfuscated) | `./gradlew.bat build` |
| `26.1` | 26.1 line (26.1 / 26.1.1 / 26.1.2) | Mojang (unobfuscated) | `./gradlew.bat build` |
| `legacy/1.21.11` | 1.21.11 | intermediary (Stonecutter) | `./gradlew.bat :1.21.11:build` |

The 26.x branches require a **JDK 25** Gradle daemon and Fabric loader **≥ 0.19.3**. On `legacy/1.21.11`
a single harmless `Cannot remap … class_742` warning is expected. Testing is manual in-game.

## Documentation

- **[OVERVIEW.md](OVERVIEW.md)** — feature catalog, architecture, and design decisions.
- **[TECHNICAL.md](TECHNICAL.md)** — code-level reference: config schema, every mixin, keybinds, commands.

## License

CC0.
