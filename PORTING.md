# TurtMod — Forward-Porting Plan (to newer Minecraft versions)

Goal: port TurtMod from its **1.21.11 base** to each newer MC release cleanly, with one shared
codebase, minimal regressions, and a repeatable checklist. Built on the existing Stonecutter
multi-version setup.

> **Versioning note:** after 1.21.11, Minecraft uses a **calendar scheme**: `26.1` (major) →
> `26.1.1`, `26.1.2` (patches) → `26.2` (next major), etc. These are **already released**. Target
> order: `1.21.11 (base) → 26.1 → 26.1.1 → 26.1.2 → 26.2 → …`.
> - **`26.X` (new major, e.g. 26.1, 26.2):** start of a new version line — expect a **Tier‑1 rewrite**
>   of several render mixins plus possible `class_332`/input/registry churn. Big effort.
> - **`26.X.Y` (patch, e.g. 26.1.1):** usually dep bumps + a handful of intermediary spot-fixes.
>   Still re-verify, because even patches can renumber intermediary ids if a class was touched.

> **Standing rules (unchanged):** mixins use pure **intermediary** names verified against the *target
> version's* decompiled jar — never yarn, never guessed. Do **not** run the client (manual in-game
> testing by the user). Pre-existing harmless `Cannot remap` warnings are expected.

---

## 0. One-time readiness (do before the first new version)

1. **Confirm Stonecutter is the source of truth.** Versions are declared in `settings.gradle(.kts)`;
   per-version deps live in Stonecutter property files. The 1.21.11 node stays the `vcsVersion`
   (active/base) so the working tree always compiles against base.
2. **Gating dependency check FIRST** (this is the real go/no-go, bigger than mappings):
   - **WalksyLib** — the entire config UI depends on it. A version is **blocked** until WalksyLib
     publishes a build for it (or we self-build / swap the UI). Check Modrinth/Maven before coding.
   - **ukulib** — transitive runtime dep of WalksyLib; carry whatever WalksyLib needs, nothing more.
   - fabric-api, fabric-language-kotlin, cloth-config, modmenu, yarn/intermediary, loader, loom —
     straightforward coordinate bumps; lift exact values from the official `fabric-example-mod`
     `gradle.properties` for that MC version.
   - DiscordIPC bundled jar — version-independent, no action.
3. **Decompiled-jar workflow** (per version): after the first `:<ver>:` configure, the intermediary
   sources jar lands under
   `.gradle/loom-cache/minecraftMaven/.../<ver>-…intermediary…-sources.jar`.
   Extract targets on demand: `unzip -o "$JAR" "net/minecraft/class_XXXX.java" -d _mcref`
   (delete `_mcref` when done — it's scratch).

---

## 1. Per-version porting procedure (repeat for each new MC version)

### Step A — Add the node & bump deps
1. Add the version to the Stonecutter list; set its per-version properties from the Fabric template.
2. Run `./gradlew :<newver>:build`. Collect **every** failure: compile errors, mixin
   apply/remap failures, missing classes.

### Step B — Re-resolve broken intermediary targets
For each failure, open the **new version's** decompiled jar and re-resolve the
`class_/method_/field_` (and any inlined constant, e.g. a tab-list slot width or a `field_` opcode in
an `@ModifyExpressionValue`). Patch behind a Stonecutter guard so other nodes are untouched:
```java
//? if >=26.1 {
/*<new-version code>*/
//?} else {
<base 1.21.11 code>
//?}
```
(Stonecutter compares the declared version strings; declare nodes as `"1.21.11"`, `"26.1"`, … and
guard with those exact tokens.)
Prefer guarding the **smallest unit** (a target string, a constant, one method) over whole files.

### Step C — Build green, then validate
- `:<newver>:build` passes with only the known harmless remap warnings.
- User manual in-game test (see §5). Only then advance to the next version.

---

## 2. Risk map — what breaks, ordered by likelihood (this mod specifically)

History: Mojang renumbers/refactors render & GUI internals almost every release; logic/world code is
usually stable. Watch these TurtMod mixins, hardest first:

### Tier 1 — Very likely to break each version (render pipeline)
- `EquipmentLayerRendererMixin` (class_10197, `@ModifyExpressionValue` on `class_4608.field_21444`
  + `@WrapOperation` on `class_12249.method_75966/75964/75973`) — armor/trim tint; deeply tied to
  the equipment render rewrite.
- `EntityRenderDispatcherMixin` / `DebugHitboxCleanupMixin` (class_12155, class_12180 hitbox
  builders, `method_75432`) — the hitbox API is new and churny.
- `GlintTranslucentMixin` (class_1921 `getGlintTranslucent`) + `ItemRendererMixin`
  (class_918 `method_23181`/`getArmorGlintConsumer`) — glint recolor.
- `LightmapTextureManagerMixin` (class_765 `method_3313`, 6× `Std140Builder.putFloat` args) —
  fullbright; brittle (arg ordinals).
- `ShieldModelRendererMixin` (class_10509 `method_65707`), totem-pop chain
  (class_4603 `method_70939`, class_11224 `method_70865`, class_759 `method_3198`).
- Fog: `FogEnvironmentMixin` (class_11401/11402/7283/7284/11403/11398) + `FogRendererMixin`
  (class_758). The fog system has a **boundary at 1.21.6**; future versions may move it again.

### Tier 2 — Likely (GUI / HUD / input)
- `InGameHudMixin`, `DebugHudMixin` (class_340 `method_1846`), `PotionHudMixin`,
  `PlayerListPingMixin`/`PlayerTabOverlayMixin` (class_355 `method_1919/1918`, `@ModifyConstant`).
- `class_332` draw-method signatures (used all over the custom UI: `method_25290/25302/25293`,
  `method_27535`, `method_51445`, `method_52706`). A `class_332` change ripples through `ui/`.
- Input: `class_309` Keyboard (`method_1468`), `class_312` Mouse (`method_1606`), `class_11908`
  KeyEvent — the gamemode switcher + zoom/freelook depend on these.

### Tier 3 — Occasional (logic/codec/registry)
- `KitSerializer` (ItemStack CODEC, `class_5455` registries, NBT getters `method_68564/68083`) —
  codec/NBT API shifts.
- Permission API `class_12090/class_12096` (`GameModeSwitcher*Mixin`) — new since 1.21.10; a future
  rework would need re-guarding.
- Particle preview (`RegistryPickerScreen.particleFrames`, ResourceManager + texture JSON).

### Tier 4 — Rarely (stable)
- Config model, command tree, keybind registration, Discord RPC, most `utils/`.

---

## 3. Stonecutter guarding patterns

- **Single target string differs:** guard just the `@At(target=...)` or `method=...` string.
- **Method renamed:** keep two `@Inject`/`@Redirect` methods, each guarded; or use a guarded
  `@Dynamic` with version-specific selectors.
- **Class replaced (e.g. fog system boundary):** guard the whole mixin and register it conditionally
  in `turtmod.client.mixins.json` via a Stonecutter-processed resource, or split into
  `FooMixin` (new) / `FooLegacyMixin` (old) each guarded.
- **API moved packages / signature changed in feature code:** wrap the call site, not the whole
  feature, so logic stays single-sourced.
- Keep the **1.21.11 branch as the default/else** so the base build is never disturbed.

---

## 4. Tooling & accuracy

- **Mapping lookups:** use Linkie / the decompiled jar to map yarn↔intermediary when reading
  references from other mods — but commit only intermediary.
- **Don't trust memory across versions:** an intermediary id stable in 1.21.11 can be renumbered in
  the next release whenever its class is restructured. Re-verify every flagged target.
- **Diff the decompiled class** between base and target to spot signature/inline-constant changes
  fast (`diff old/class_X.java new/class_X.java`).

## 5. Verification gate (per version)
- `./gradlew :<ver>:build` (then `chiseledBuild`) green, only known remap warnings.
- Manual in-game (user): config UI opens (WalksyLib loaded), HUD editor drags every anchor, render
  features (fog/shield/hitcolor/glint/fullbright/totem) behave, Clean F3 + chat + tab/nametag ping
  work, an existing `turtmod.json` loads (migration intact), Kit/Skin/Gallery screens open.
- **Regression guard:** the 1.21.11 base node must still build & behave identically afterward.

## 6. Recommended order & cadence
`1.21.11 (base) → 26.1 → 26.1.1 → 26.1.2 → 26.2 → …`. Do **one** at a time; ship/validate before the
next. **`26.1` and `26.2` are major** (new version line) → likely **Tier‑1 render rewrite**
(equipment/hitbox/glint/lightmap/fog/shield/totem) plus possible `class_332`/input/codec churn.
**`26.1.1`/`26.1.2` are patches** → usually dep bumps + a few intermediary spot-fixes, but still
re-verify every flagged target against that exact jar.

## 7. Per-version checklist (copy per release)
```
[ ] WalksyLib (+ukulib) build exists for <ver>?  (blocker if not)
[ ] dep coords bumped from fabric-example-mod <ver>
[ ] :<ver>:build — collect all failures
[ ] re-resolve each broken class_/method_/field_ vs decompiled <ver> jar
[ ] guard deltas with Stonecutter (smallest unit), 1.21.11 stays default
[ ] build green (only known remap warnings)
[ ] user in-game test passes (config, HUD editor, render feats, chat/tab, kit/skin/gallery)
[ ] 1.21.11 base still builds + behaves identically
[ ] tag / changelog
```

## 8. Open blockers to track
- WalksyLib availability per new version (the real go/no-go).
- The 1.21.9/1.21.10 **backport** is separately blocked only by the permission API guard
  (`class_12090/12096`) — unrelated to forward porting but finish it so `chiseledBuild` is green.
