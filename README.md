# 🐢 TurtMod

**TurtMod** is a client-side Fabric mod for Minecraft **1.21.11** that bundles dozens of visual, combat, and HUD quality-of-life tweaks into one place. Every feature is a toggle you control, and on a fresh install everything starts **off** — enable exactly what you want.

> Client-side only. Works on any compatible Fabric installation.

## ✨ Features

- **Visual** — Fullbright, Low Fire, Shield tweaks (recolor/resize/offset), per-fog controls, overlay removal (pumpkin blur, powder snow, darkness, portal), item scaling, block-outline recolor, hit color.
- **Combat & HUD** — Custom hitboxes with **per-entity colors**, player health indicator, movable HUDs (armor, potions, keystrokes, CPS, coordinates, FPS/ping) with a HUD editor, Clean F3.
- **Utility** — Zoom, freelook, clear view, particle & sound tweaks, command keys, screenshot gallery & editor, kit loader, skin & cape changer, Discord RPC.

Everything is configurable in-game via **Mod Menu** (or `config/turtmod.json`), with a built-in color picker.

## 📦 Installation

1. Install **Fabric Loader** for Minecraft 1.21.11.
2. Add the dependencies to your `mods/` folder: **Fabric API**, **Fabric Language Kotlin**, **Cloth Config**, **Mod Menu**.
3. Drop the TurtMod `.jar` into `mods/`.
4. Launch with the Fabric profile.

## 🔧 Building from source

```bash
./gradlew build
```

The built jar is written to `versions/1.21.11/build/libs/`.

## 💚 Credits & Thanks

TurtMod was inspired and helped by several amazing mods and their creators. Huge thanks to every project whose ideas and hard work shaped this one, and to the wider Fabric modding community. 🙏

Thank you to everyone who tested TurtMod and sent feedback — and a **big thank you to Veqtora** for all the help along the way. 💛

## 📜 License

Licensed under the **GNU General Public License v3.0** — see [`LICENSE`](LICENSE). This mod reuses ideas and, where applicable, code from other open-source Fabric mods; GPL-3.0 keeps the project open and ensures those contributions are credited and shared alike.
