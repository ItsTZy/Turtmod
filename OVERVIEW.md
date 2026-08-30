# TurtMod — Detailed Overview

> A client-side Minecraft (Fabric, 1.21.11) quality-of-life mod focused on **visual clarity,
> combat feedback, and HUD customization** — with a custom "launcher-style" config UI, cosmetics,
> a screenshot gallery, command-key macros, and a kit loader.

---

## 1. At a glance

| | |
|---|---|
| **Mod id** | `turtmod` |
| **Name** | TurtMod |
| **Environment** | Client only |
| **MC version** | **1.21.11** — this `legacy/1.21.11` branch (Stonecutter, intermediary mappings). Sibling branches carry the 26.x line: `master` = 26.2, `26.1` = the 26.1 line (both Mojang-unobfuscated). Old 1.21.9/1.21.10 nodes were dropped. |
| **Loader** | Fabric (loader ≥ 0.18.4, loom 1.16) |
| **Language** | Java 21 (+ Fabric Language Kotlin runtime) |
| **Entry point** | `com.turtmod.TurtModClient` (`ClientModInitializer`) |
| **Config UI** | Fully native — a custom module-grid screen + a native launcher-style settings screen (no third-party config lib) |
| **Source files** | ~145 Java classes |
| **Mixins** | ~42 client mixins/accessors |
| **Maven group** | `com.tzy.turtmod`, version `1.0.0` |

**Bundled / required deps:** fabric-api, fabric-language-kotlin, cloth-config, modmenu, and a jar-in-jar **DiscordIPC 0.11.3** (Discord Rich Presence). The config UI is 100% native — **WalksyLib and ukulib have been removed** (no longer a dependency or jar-in-jar bundle).

---

## 2. Architecture

```
com.turtmod
├─ TurtModClient            entry point: registers keybinds, commands, tick loop, screens
├─ config/                  config model, persistence, all config screens
├─ ui/                      custom UI toolkit (Palette, TurtLauncher chrome, widgets, utils)
│  └─ config/               native config model + the native settings screen (replaces WalksyLib)
├─ hud/                     all HUD render features + the HUD editor
├─ visual/                  fullbright, freelook, zoom, hurtcam, screen effects, tinting
├─ combat/                  player health indicator, hit color / armor tint
├─ chat/                    better chat, screenshot tools/upload
├─ cosmetics/              skin changer (3D preview) + cosmetic profiles
├─ gallery/                 screenshot gallery + viewer
├─ kit/                     kit save/load/preview (inventory snapshots)
├─ discord/                 Discord Rich Presence service
├─ utils/                   CPS tracker, ping colors, shield tracking, image clipboard, logger
└─ mixin/client/            ~44 mixins hooking render/HUD/input/network
```

### Config system (`config/`)
- **`TurtModConfig`** — a plain data object with nested groups: `Visual`, `Combat`, `Hud`, `CustomTheme`, `Misc` (plus helpers `ShieldColorConfig`, `CommandKey`/`CmdMsg`, `PotionSortMode`).
- **`ConfigManager`** — GSON load/save to `config/turtmod.json`; loads bundled `turtmod.default.json` on first run; **auto-recovers** (backs up + resets) on corrupt JSON; runs forward-compat `migrate()` (e.g. command-key macros, clean-F3 order/colors) so new fields never wipe old configs.
- Two config front-ends:
  - **`TurtModClientConfigScreen`** — the custom **module grid** (tabs: Visuals / HUD / Utility / Misc). Left-click a card toggles it, right-click opens its settings.
  - **`TurtModConfigScreenFactory`** (was `TurtModWalksyConfigScreenFactory`) — builds the native config tree (categories / option groups / typed options) and opens **`TurtNativeConfigScreen`** for the full settings page + per-module settings pages (sliders + click-to-type number fields, HSB color picker, enum pills/dropdowns, buttons).
- **Native config model** (`ui/config/model/`) — `LocalConfig` / `Category` / `OptionGroup` / `Option<T>` / `OptionDescription` / `ConfigColor`: a self-contained reimplementation that replaced WalksyLib's builder API.

### UI toolkit (`ui/`)
- **`Palette`** — single source of theme colors.
- **`TurtLauncher`** — shared "chrome": rounded panel, header w/ logo + gradient title, sidebar, footer, content helpers (`drawContentPanel`, `drawSectionTitle`).
- **`TurtUIUtils`** — drawing primitives: rounded rects/borders (cheap O(r) corner-cut — intentionally not anti-aliased, for performance), gradients, soft glows, the reusable **presentation `drawStage`**, animated menu backdrop, eased lerp.
- **`TurtNativeConfigScreen`** (`ui/config/`) — the native settings screen: horizontal category tabs, animated toggles, sliders with click-to-type fields, enum dropdowns, an improved HSB color picker (SV square + hue/alpha bars, checkerboard, presets, hex readout), all inside the shared `TurtLauncher` chrome.
- Widgets: `TurtUIButton`, `TurtUICheckbox` (module cards w/ turtle-shell sprites), `TurtUILabel`, `TurtUIPanel`, `TurtUIScale` (resolution-independent logical layout), `TurtUITheme`.
- `BrandingRenderer` / `TurtLogoButton` — logo rendering.

---

## 3. Feature catalog

### Visual (`visual/` + many mixins)
- **Fullbright** — adjustable gamma/brightness override.
- **Freelook** — hold-key free camera without turning the player.
- **Zoom** — toggle/hold, smooth, sensitivity-normalized, hides arms.
- **Fog tweaks** — disable/limit water, lava, nether, blindness, powder-snow, darkness, and atmospheric fog; custom fog distance/density and per-type start/end.
- **Overlay removal** — pumpkin blur, powder-snow, darkness, nausea distortion, fire overlay (+ Y offset).
- **Hurt cam** — old/new hurt shake styles, intensity %, disable screen shake.
- **Block outline recolor** (color, width, alpha, invert-depth, rainbow).
- **Shield tweaks** — recolor by usable/broken/using state, custom size/offset, self-only, grayscale, per-player colors, "only show when blocking", third-person eating.
- **Held item tweaks** — scale %, per-hand custom **scale / position / rotation (X/Y/Z)** for main & off hand.
- **Small totem** — scale + offset the totem in hand.
- **Totem pop tweaks** — pop animation scale/offset, remove swirl particles, crystal/explosion particle scale.
- **Projectile trails** — colored trails with length/lifetime/alpha controls.
- **Own nametag** — render your own nametag.
- **Custom nametags** + ping-on-nametag.
- **TNT timer**, **smooth sneak**, **fishing bobber hide / rod overlay**.

### Combat (`combat/`)
- **Player health indicator** — shows other players' health (hotbar-style hearts or number), works on invisible/armor-only, configurable hearts, scale, Y-offset, exact number.
- **Hit color** — flash entities on hit (color, alpha, rainbow); armor damage tint + armor trim tint.

### HUD (`hud/`) — all movable via the HUD editor
- **Armor HUD** (styles, durability, warnings, side, offhand/mainhand).
- **Potion HUD** (styles ICONS/COMPACT/FULL, columns, max rows, sort modes, anchored so it doesn't drift).
- **FPS/Ping overlay**, **Reach display**, **Keystrokes**, **CPS counter**, **Sprint/sneak display**, **Inventory HUD**, **Coordinates HUD**, **Elytra pitch HUD**.
- **Clean F3** — BetterF3-style replacement: per-line `name: value` two-tone shadowed text on per-line translucent backgrounds; reorderable lines; toggles for FPS(+min/max)/ping, XYZ, block, chunk, light, facing, speed, biome, dimension, day/time, held item, memory, looking-at.
- **Custom hitboxes** — recolor, target/hurt colors, max distance, reveal invisible (players / armor-only / other entities), hide fireworks; debug-hitbox cleanup.
- **HUD editor** (`HudEditorScreen`/`HudEditorFeature`) — drag every panel, snap-to-grid/center, per-anchor scale, reset.
- **Custom theme renderer** — themed boxes/text/colors/scale shared by all HUD panels.

### Utility / Misc
- **Command Keys** — up to 8 bindable **macros**, each a sequence of messages/commands with **per-line delays** and a mode: **SEND / CYCLE / REPEAT**, plus send-vs-type-in-chat. Editable via GUI or `/turtmod cmdkey`.
- **Kit Loader** — save/equip full inventory snapshots (SNBT); preview kits (3D-accurate item icons, works in the main menu); `/turtmod kit` commands + Kit Manager screen.
- **No-Op Gamemode Switcher** — F3+F4 opens the switcher without local op and applies via `/gamemode` (needs server permission to take effect).
- **Hide Particles** / **Mute Sounds** — module GUIs with searchable registry pickers (particles show **live animated thumbnails**; sounds have a play-preview), plus master toggles and quick mute presets (anvil, note blocks, totem pop, XP orb).
- **Death coords** — captures death location + dimension; chat message + on-screen panel with Copy Coords / Respawn+TP / View Items buttons.
- **Hide scoreboard**, **container buttons**, **clean debug hitboxes**.

### Chat & Screenshots (`chat/`, `gallery/`)
- **Better chat** — compact/dedupe, hide chat, timestamps, input suggestor tweaks.
- **Screenshot tools** — actions on screenshot toast (copy/upload), `/turtmod screenshot` + upload commands.
- **Screenshot Gallery** — uniform 16:9 grid, thumbnails, view/open/copy/delete/folder; full-image viewer.

### Cosmetics (`cosmetics/`)
- **Skin Changer** — browse local skins, live **3D player-model preview** on a presentation stage (drag to orbit), classic/slim model, apply to your Mojang profile (upload), import via native file picker, per-row mini-stage previews.

### Integrations
- **Discord Rich Presence** (`discord/`) — username, server name, dimension display toggles.
- **ModMenu** integration → opens the TurtMod config.
- **Title screen** branding (logo button → TurtMod main menu).

---

## 4. Modules (toggleable units)

`FULLBRIGHT, FREELOOK, HIT_COLOR, LOW_FIRE, LOW_SHIELD, FOG_CONTROLS, OVERLAYS, CAMERA_SETTINGS,
BLOCK_OUTLINE, SMALL_TOTEM, DISCORD_RPC, HELD_ITEM, ARMOR_HUD, POTION_HUD, FPS_PING, REACH,
KEYSTROKES, CPS_COUNTER, SPRINT_HUD, INVENTORY_HUD, CUSTOM_HITBOXES, SCOREBOARD, BETTER_SCREENSHOT,
CLEAN_F3, HEALTH_INDICATOR, ZOOM, THEME_SETTINGS, ELYTRA_HUD, OWN_NAMETAG, COORDINATES_HUD,
PING_DISPLAY, DEATH_COORDS, MUTE_SOUNDS, HIDE_PARTICLES, COMMAND_KEYS, KIT_LOADER, GAMEMODE_SWITCHER`

Each module can be toggled from the grid, has a settings page, an optional **toggle keybind**, and a **per-module "Reset to Defaults"**.

---

## 5. Keybinds (all rebindable in MC Controls)
- Open Config (default **N**), Freelook (hold, default **Right Alt**), Zoom.
- Health Indicator: open config, toggle, offset up/down, reset.
- 8 × Command Key slots.
- A toggle key per module (unbound by default).

## 6. Commands (`/turtmod …`)
- `cmdkey set|clear|list`
- `kit save|load|delete|preview|list`
- `particle hide|show|list|clear`
- `sound mute|unmute|list|clear`
- `screenshot`, `uploadlastscreenshot`
- (Kit/gallery/skin actions also available via their screens.)

---

## 7. Mixins (high level, `mixin/client/`)
- **Render:** Fog (legacy + environment), EquipmentLayerRenderer, LivingEntityRenderer, PlayerEntityRenderState, FlyingItemEntity/HeldItemFeature, ShieldModelRenderer, WorldRenderer, GameRenderer, Camera, Lightmap, totem-pop renderers.
- **HUD:** InGameHud, DebugHud (Clean F3), PotionHud, PlayerTabOverlay/PlayerListPing.
- **Input/screens:** Mouse, GameModeSwitcher (open + apply bypass), GameMenuScreen, HandledScreen, ChatScreen/Suggestor/Timestamp, Title/Splash screens.
- **World/entity:** ClientWorld, ClientPlayNetworkHandler, Entity/Player, EntityNameTagPing, hide-particles/mute-sounds, hit color, fishing bobber.
- **Accessors/invokers** for otherwise-private fields/methods.

---

## 8. Notable design decisions & limitations
- **Pure intermediary mappings** in mixins (`class_/method_/field_`) on this `legacy/1.21.11` branch — every name verified against the decompiled 1.21.11 jar. The 26.x branches (`master` = 26.2, `26.1`) use Mojang-unobfuscated names instead.
- **Gamemode switcher** can bypass the *client* gate but still needs real server `/gamemode` permission (a hard limit, same as the source mod).
- **Kit/skin preview** needs world registries for full fidelity (enchants/potions); a cold main menu falls back to base item icons (it caches registries after you join any world).
- **Particle preview** reads each particle's texture JSON (no clean sprite API in 1.21.11); particles without a static texture can't show a thumbnail.
- **Native config:** the settings UI no longer depends on WalksyLib/ukulib — it's a self-contained model + screen, which also unblocked the multi-version port (WalksyLib's per-version availability was the #1 blocker).
- **Three supported versions, three branches:** this `legacy/1.21.11` = 1.21.11 (Stonecutter, intermediary), `master` = 26.2, `26.1` = the 26.1 line (both Mojang-unobfuscated, no-remap loom). The old Stonecutter multi-node effort (1.21.9/1.21.10) was dropped.
- **Removed feature:** the Totem/Potion counter modules were removed from all branches after they never worked reliably.

---

## 9. Build
```bash
# this branch (legacy/1.21.11) — Stonecutter, intermediary
./gradlew.bat :1.21.11:build

# the 26.x branches (master = 26.2, 26.1) — Mojang-unobfuscated, JDK 25 daemon
./gradlew.bat build
```
On this branch, a pre-existing harmless `Cannot remap …` warning (class_742) is expected.
Testing is manual in-game (no automated client run).
