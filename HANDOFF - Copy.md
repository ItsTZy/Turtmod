# TurtMod Handoff — May 2026

## Session 2026-05-30 — Fixes & Corrections

Verified against the decompiled 1.21.11 intermediary sources (loom cache jar). Build green.

| Issue | Root cause | Fix |
|-------|-----------|-----|
| **Shield recolor flickered AND lit other players red at random** | TWO bugs. (1) Render: `ShieldModelRendererMixin` reimplemented `class_10509.method_65707` with the OLD direct-buffer API (`method_23000()` + `method_22699`); 1.21.11 renders via the deferred submit-queue `class_11659`, so drawing outside it wrote depth out of order → flicker. (2) Detection: `ShieldTracker` GUESSED cooldowns — "break sound near ANY shield holder" + "player stopped blocking ⇒ assume break" — causing random red on others. | (1) Submit through the queue: `queue.method_73483(matrices, layer, (entry,vc)->…)` (submitCustom), layer `class_12249.method_76000` (entity_translucent), per `repos/ShieldStatus-1.21.9-1.21.10`. (2) Rewrote `ShieldTracker` to read the player's REAL `class_1796` item-cooldown manager (`method_7357().method_7904/method_7905`); other players' cooldowns are written (`method_62835`) ONLY on entity-status-30 or a break-sound within 5 blocks of a player we hit with an axe while they were blocking. False positives gone. |
| **"Reset All" (Settings tab) did nothing** | Button called `ConfigManager.reset()` then `ConfigManager.load()` but **discarded** the return value — the live `TurtModClient.config` was never reassigned. | Added `TurtModClient.reloadConfig()` (re-reads disk into the static field + re-bootstraps RPC); button now calls it. |
| **"Reset Scale" (HUD editor) did nothing** | In `HudEditorScreen.method_25402`, `HudEditorFeature.mouseClicked` ran BEFORE the UI buttons. Clicking the button (not on a HUD element) set `selected = null`, so `resetSelectedScale` had nothing selected. | Check UI buttons first, then fall through to `HudEditorFeature.mouseClicked`. |
| **HUD editor header/footer too tall** | Header bar 28 px, footer strip 32 px (buttons 20 px). | Header → 18 px, footer → 22 px (buttons 14 px), via `HEADER_H`/`FOOTER_H` constants. |
| **"Potion Throw Counter" (potcounter — health-pot pops) module missing** | Config fields + settings page (case 32) + ModuleKind existed, but no tile in the module grid and no `getModuleKindForCheckbox` route — so it looked merged with the inventory "Potion Counter" (potioncounter). | Added the "Potion Throw Counter" tile to the Combat tab (`cfg.combat.potionThrowCounterHud`) + routing case. The two are now distinctly visible: **Potion Counter** = inventory potions (hud/PotionCounterFeature), **Potion Throw Counter** = thrown health-pot pop tracker (combat/PotionThrowTracker). |
| **Per-environment fog tweaks were dead code + mismapped** | `FogEnvironmentMixin` was never registered in `turtmod.client.mixins.json`, and its `instanceof` dispatch was mislabeled. | Registered the mixin. Corrected the verified map (via `method_42593`/`method_42594`): `class_11401`=Lava, `class_11402`=PowderSnow, `class_11403`=Water, `class_7283`=Blindness, `class_7284`=Darkness. Darkening cancel (`method_42592`) now targets `class_7284` (Darkness), not `class_11403`. |

| **HUD editor had no open animation** | — | Added a fade-in-from-black on open (`openFade` eased via `TurtUIUtils.lerp01`), matching the other screens. |
| **Block outline was a flat recolor only** | — | Ported CustomBlockHighlight extras onto the existing `WorldRendererMixin` hook: Line Width + Opacity sliders and an animated **Rainbow** outline (+ speed). (Full fill/fade/outline-modes need a custom geometry renderer — deferred.) |

> 2026-06-01 update: `ShieldStatusManager.java` (utils/) was deleted. The live shield system is `ShieldTracker.java`, wired into tick + network handlers + the renderer mixin.

### Build-break correction (later same session)
A subsequent assistant added `ItemInHandRendererMixin.java` (a CookeyMod-style "Tool Blocking / Swing-and-Use" first-person animation) and claimed a green build, but it did **not** compile: it was written against the pre-1.21.11 API (shadowed `class_759.method_3231/method_3233/method_3216` with `class_4597` VertexConsumerProvider signatures, treated `field_4052` as an item when it is a `float`, and imported the non-existent `com.mojang.math.Axis`). 1.21.11 renders the first-person item through the `class_11659` submit queue, so those signatures don't exist. It was also never registered in `turtmod.client.mixins.json`. **Fix:** removed the broken file to restore the build; also de-duplicated a doubled `PlayerTabOverlayMixin` entry in the mixin config. The `cfg.visual.toolBlocking` / `swingAndUse` config fields + their toggles on the Hurt Cam settings page remain but are currently **no-ops** — a correct queue-based reimplementation is still TODO.

Features 1–5 of the plan are otherwise present and the build is green: Coordinates HUD module, combat-hitbox upgrades (double outline + far line width + force-vanilla-F3+B toggle via `DebugRendererMixin`), Pearl Detector (`FlyingItemEntityRendererMixin`), totem/pot tab-list + text-display nametag counts (`PlayerTabOverlayMixin` = class_355.method_1918, `TextDisplayRendererMixin` = class_8138$class_8141.method_49057), and UI polish (module-card on/off accent, tab slide-in).

## What We Built

### Repos Referenced
| Repo | Source | Feature |
|------|--------|---------|
| `repos/totemcounter-1.21.11/` | uku3lig | Totem pop counter (nametag, tab, XP bar, HUD, round-end reset, commands) |
| `repos/potioncounter-1.21.11/` | uku3lig | Inventory potion counter (all splash potions by type, icons+counts grid) |
| `repos/potcounter-1.21.11/` | skalpha | Health potion pop tracker (PvP — tracks thrown health pots per player, nametag, tab, XP bar, round-end reset) |
| `repos/CustomBlockHighlight-main/` | — | Block outline customization |
| `repos/clearviews-multiloader-mainfr/` | — | Fog customization |
| `repos/bactromod-main/` | — | Shield status UI customization |

---

## All Files Changed/Created

### NEW Files

| File | Purpose |
|------|---------|
| `src/main/java/com/turtmod/combat/PotionThrowTracker.java` | Health potion pop tracker — `Map<UUID, Integer>`, `increment()`, `get()`, `reset()`, `appendPots()` for nametag injection, `render()` for HUD, `potsColor()` for color thresholds. Fully parallel to `TotemPopTracker`. |
| `src/main/java/com/turtmod/mixin/client/TotemPopGameRendererMixin.java` | Self-pop detection via `GameRenderer.method_3198` (item activation). Increments own UUID when totem activates. |
| `src/main/java/com/turtmod/mixin/client/TotemPopXpBarMixin.java` | Colored XP bar overlay for totem pops. Paints filled bar with pop-color gradient behind vanilla bar. Supports both Totem and Potion counts. |
| `src/main/java/com/turtmod/mixin/client/TotemPopRoundEndMixin.java` | Round-end auto-clear for both `TotemPopTracker` + `PotionThrowTracker`. Checks chat messages against `ROUND_END_MESSAGES` list. |
| `src/main/java/com/turtmod/mixin/client/PotionThrowEntityDataMixin.java` | Health potion throw detection via `ClientPacketListener.method_11093` (handleSetEntityData). Checks entity is `AbstractThrownPotion` with `INSTANT_HEALTH` effect. |
| `src/main/java/com/turtmod/mixin/client/PlayerTabOverlayMixin.java` | **Feature 4 Overhaul:** Wires pop/pot counts into the Tab list. Uses `method_1918` (getNameForDisplay) to inject text. |
| `src/main/java/com/turtmod/mixin/client/TextDisplayRendererMixin.java` | **Feature 4 Overhaul:** Wires pop/pot counts into 1.21.11 `TextDisplay` nametags. Targets `class_8138$class_8141.method_49057` to modify displayed text. |

### MODIFIED Files

| File | Changes |
|------|---------|
| `src/main/java/com/turtmod/TurtModClient.java` | Added `PotionThrowTracker.render()` call in `onHudRender`. Added disconnect handler to reset `PotionThrowTracker`. Import for `PotionThrowTracker`. |
| `src/main/java/com/turtmod/config/TurtModConfig.java` | Added combat fields: `totemShowPopCounter`, `totemColoredXpBar`, `totemAlwaysShowXpBar`, `totemShowInTab`, `totemSeparator`, `totemCounterColors`, `potionThrowCounterHud`, `potionThrowNametagPots`, `potionThrowShowInTab`, `potionThrowColoredXpBar`, `potionThrowAlwaysShowXpBar`, `potionThrowSeparator`, `potionThrowCounterColors`. |
| `src/main/java/com/turtmod/config/TurtModWalksyConfigScreenFactory.java` | Added `POTION_THROW_COUNTER` to `ModuleKind` enum. Added display name + config page (case 32) with all 7 toggles. Updated Totem Counter page (case 27) with 6 new toggles. Added Totem Counter + Potion Throw Counter option groups to main Combat category. |
| `src/main/java/com/turtmod/combat/TotemPopTracker.java` | Added `ROUND_END_MESSAGES` list, `getPops()`, `popColor(int)` with color thresholds (green→yellow→gold→red). Config-driven separator/color support in `appendPops()`. |
| `src/main/java/com/turtmod/combat/TotemCounterFeature.java` | Fixed render method — removed fake `method_51469()`/`method_22911()`, replaced with proper `pushMatrix()`/`popMatrix()`. |
| `src/main/java/com/turtmod/ui/TurtUIModuleCard.java` | **Feature 5 Polish:** Added state-dependent accent bar (left edge). ACCENT_GREEN when enabled, Dark Gray when disabled. Increased hover glow intensity. |
| `src/main/java/com/turtmod/config/TurtModClientConfigScreen.java` | **Feature 5 Polish:** Implemented `tabFade` easing (0→1) on tab switch to create a horizontal slide-in effect for the module grid. Enhanced Search bar with rounded backgrounds and hover/focus borders. |
| `src/main/resources/turtmod.client.mixins.json` | Registered all new mixins: `PlayerTabOverlayMixin`, `TextDisplayRendererMixin`, `TotemPopXpBarMixin`, etc. |

---

## Technical Deep Dive: Feature 4 & 5 Overhaul

### Feature 4: Totem/Pot Counter Integration (1.21.11)
The primary challenge was locating the correct intermediary hooks for nametags and tab-lists in the new 1.21.11 client.

1. **Nametags (TextDisplay):** 1.21.11 uses `class_8138$class_8141` (TextDisplayRenderer$TextLine) to process lines. We target `method_49057` to intercept the `class_2561` (Text) object. We use `getString()` to scan the raw content for player names and append `-N` counts via `TotemPopTracker` / `PotionThrowTracker`.
2. **Tab List:** Targeted `class_355` (PlayerTabOverlay) `method_1918` (getNameForDisplay). This injection ensures that when the client renders a player's name in the tab list, it includes the tracked pop/potion counts.
3. **Colored XP Bar:** The `TotemPopXpBarMixin` was updated to check both totem and potion trackers. If both are present, it prioritizes the one with the lower count (higher threat) or specifically configured settings. It now supports the "Always Show" config option, allowing the colored bar to stay visible even when the vanilla XP bar would normally hide.

### Feature 5: UI Polish & Aesthetics
The UI was overhauled to feel more "alive" and modern.

1. **Module Cards:** Each card now has a 2px vertical accent bar on the left. The color is dynamically tied to the `enabled` state: a vibrant green (0x55FF55) when ON, providing a much stronger visual cue than just the "ON/OFF" label. Hovering now uses a stronger glow multiplier (1.2x) and faster easing for snappier feedback.
2. **Config Screen Transitions:** Swapping tabs in the settings screen now triggers a `tabFade` animation. The module grid is translated horizontally based on `(1f - tabFade) * 14f`, making the new tab's modules "slide" into place.
3. **Search & Consistency:** The search bar was updated from a flat rect to a rounded rect with specialized border colors for focus (ACCENT_PINK) and hover (ACCENT_GREEN), matching the main UI theme.

### Build Fixes (Compilation)
- **GameProfile API:** In 1.21.11, the `authlib` `GameProfile` uses `id()` instead of `getId()`.
- **Text Content:** `class_2561.method_10851()` returns a `class_7417` (Content) object in 1.21.11, not a String. Used `getString()` (inherited from `class_5348`) to get the flat representation.
- **Imports:** Resolved missing `PotionThrowTracker` imports in mixin classes.

---

## Features Ready for Testing
### ✅ Working
1. **Totem Counter HUD** — shows totem count or pop count at configurable position
2. **Totem Pop Counter** — tracks actual deaths prevented by totem per player
3. **Totem Colored XP Bar** — tinted XP bar overlay reflecting totem count. Supports "Always Show" logic.
4. **Totem Self-Pop Detection** — detects when YOU pop a totem via GameRenderer
5. **Totem Nametag Pops** — `-N` appended to player nametags (TextDisplay support added).
6. **Pot Throw HUD** — shows health pot throws count on HUD
7. **Pot Throw Detection** — intercepts entity data packets for thrown health pots
8. **Pot Throw Nametag/Tab** — `-N` appended to player nametags and tab list.
9. **Round-End Reset** — clears both trackers on game end
10. **Fog Customization** — disable all/water/lava/nether fog, adjust distance
11. **Block Outline** — custom color + alpha + width
12. **Config Screen** — all new toggles in Walksy UI
13. **UI Polish** — Module card accents (state-dependent), eased hover highlights, and slide-in transitions for settings tabs. Unified rounded corners and shadows.

### ❌ Not Wired (1.21.11 limitations)
*These features exist in the code but are not yet hooked into the 1.21.11 client:*
- **Per-fog-type start/end** — 1.21.11 FogRenderer uses GPU UBO (`class_758`), not RenderSystem calls. Values are in config but not hooked.
- **Block outline invert depth** — `RenderSystem.setDepthFunc()` was removed in 1.21.11; depth managed through GPU pipeline.
- **Screenshot Viewer** (`gallery/ScreenshotGalleryScreen.java`) — Needs a way to open from the screenshot-taken toast.
- **Custom Crosshair** — Deferred to later phase.
- **Block Highlight Fill** — Deferred to later phase.

---

## Build & Run
```
.\gradlew.bat build
# JAR at build/libs/turtmod-mod-template-1.21.11-1.0.0.jar
```
Pre-existing "Cannot remap" warnings are harmless — they're from `require=0` mixins.

To find missing class numbers in 1.21.11:
- Extract from the jar: `jar xf <sources-jar> net/minecraft/class_XXXX.java`
- Or search the decompiled files in `.gradle/loom-cache/`
- Or grep the `.gradle/loom-cache/minecraftMaven/.../mappings.tiny` file
