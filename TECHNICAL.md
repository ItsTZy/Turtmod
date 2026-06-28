# TurtMod — Technical Reference (code-level)

Deep reference for every system: entry point, the full config schema (every field + default + enum),
every mixin (target → hooked method → injector → effect), the class catalog, keybinds, and commands.
All Minecraft names are **intermediary** (`class_/method_/field_`); friendly names are given where known.

---

## 1. Build / runtime

- **Entry:** `com.turtmod.TurtModClient implements ClientModInitializer` (+ `modmenu` entry `config.ModMenuIntegration`).
- **Mixin config:** `turtmod.client.mixins.json` (client only, `JAVA_21`, `defaultRequire: 1`).
- **Defaults resource:** `turtmod.default.json`; lang `assets/turtmod/lang/en_us.json`; sprites under `assets/turtmod/textures/gui/sprites/modules/`.
- **Bundled jar:** `META-INF/jars/DiscordIPC-0.11.3.jar`.
- **Deps:** fabricloader ≥0.18.4, fabric-api, fabric-language-kotlin, cloth-config, modmenu. (**WalksyLib + ukulib removed** — config UI is fully native; see §4 `ui/config/`.)
- **Build:** `./gradlew.bat :1.21.11:build` or `chiseledBuild`. Expected harmless `Cannot remap` warnings for `class_759/918/1921/742` members.

### TurtModClient lifecycle
- `onInitializeClient()`: builds keybind categories, registers all keybinds, registers `/turtmod` command tree, registers `ClientTickEvents` tick handler, `HudRenderCallback`/screen events, `KitIO.init()`, Discord RPC, resource-reload listener.
- **Tick loop** (`onEndClientTick`): handles open-config/health keybinds, module-toggle keybinds, the 8 command-key binds (`CommandKeysFeature.trigger`) + `CommandKeysFeature.tick`, then ticks Fullbright, Zoom, HurtCam, ScreenEffects, ReachDisplay, CPSTracker, Keystrokes, CpsCounter, etc.
- Holds `class_304[] commandKeyBinds` (8) and `Map<ModuleKind,class_304> moduleToggleKeys`.

---

## 2. Config schema (`config/TurtModConfig`)

Persisted as `config/turtmod.json` via `ConfigManager` (GSON, pretty). `load()` → `loadRaw()` (defaults on first run / recover on corrupt) → `migrate()` (`misc.ensureCommandKeys()`, `hud.ensureCleanF3()`). `save()` writes JSON. Nested groups below; default values shown.

### `Visual`
- Fullbright: `fullbright = ToggleWithSlider(true, 16, 1, 32)`.
- Overlays: `disablePumpkinBlur=true`, `disablePowderSnowOverlay=true`, `disableDarknessOverlay=true`, `disableNauseaDistortion=false`, `disableFireOverlay`, `hideFireOverlay`, `hidePortalOverlay=true`, `disableSpyglassOverlay=false`, `disableVignette=false`.
- Fog: `disableLavaFog/WaterFog/NetherFog/AllFog=true`, `disableBlindnessFog=true`, `disablePowderSnowFog/AtmosphericFog/DarknessFog=false`, `fogDensityPercent=100`, `fogDistance=8`, and per-type `*FogStart/*FogEnd` floats (`-1`=auto) for water/lava/nether/blindness/powderSnow/darkness.
- Fire: `fireYOffset=-40`.
- Shield: `shieldYOffset=-1`, `shieldStatusRecolor=true`, `shieldUseUsableColor=true`, `shieldUseBrokenColor=true`, `shieldUsableColor=-16711936`, `shieldBrokenColor=-53200`, `shieldUsingColor=-11141291`, `shieldUseUsingColor=false`, `shieldColorInterpolation=true`, `shieldGrayscaleTexture=false`, `shieldSelfOnly=false`, `customShieldSize=false`, `selfShieldScale=1`, `selfShieldOffsetX/Y=0`, `othersShieldScale=1`, `showEatingInThirdPerson=false`, `onlyShowShieldWhenBlocking=false`, `attackCooldownHandOffset=0`.
- Held item: `heldItemScalePercent=100`, `heldItemTweaksEnabled`, `customHeldItemSize`, `heldItem{Scale,Pos,Rot}{X,Y,Z}`, `customOffhandHeldItemSize`, `offhandHeldItem{Scale,Pos,Rot}{X,Y,Z}`.
- Totem: `enableSmallTotem`, `totemScale`, `totemOffset{X,Y,Z}`, `totemPopScalePercent=36`, `totemPopOffset{X=1,Y=-34,Z=1}`, `disableTotemPopRotation=true`, `disableTotemPopAnimation=false`, `removeTotemSwirlParticles=false`, `explosionParticleScalePercent=100`, `crystalFlashScalePercent=100`.
- HurtCam: `hurtCamEnabled=true`, `hurtCameraShakePercent=0`, `oldHurtCameraStyle=false`, `hurtCamMode (enum)`, `disableScreenShake`, `disableHeartBlink=true`.
- Hit/armor tint: `armorDamageTint=true`, `armorDamageTintTrim=true`, `hitColor = HitColorConfig`. (Enchant-glint recolor was removed — ineffective under the 1.21.11 deferred render queue.)
- Freelook: `freelookEnabled`, `freelookSensitivityPercent`.
- Projectile trails: `projectileTrails`, `projectileTrailColor`, `projectileTrailMaxPoints`, `projectileTrailLifetimeTicks`, `projectileTrailMinDistancePercent`, `projectileTrailAlphaPercent`.
- Pearl detector: `pearlDetectorEnabled=false`, `pearlPlaySound=true`, `pearlSoundVolume=1`, `pearlSoundPitch=1`, `pearlMinDistance=3.5`, `pearlMaxDistance=1000`.
- TNT timer: `tntTimer`, `tntTimerScalePercent`, `tntTimerMaxDistance`, `tntTimerDecimals`, `tntTimerBackground`.
- Zoom: `zoomEnabled`, `zoomToggleMode`, `zoomBaseLevel`, `zoomInPerScroll`, `zoomOutPerScroll`, `zoomSmoothInOut`, `zoomHideArms`, `zoomNormalizeSensitivity`, `zoomSmoothCamera`, `zoomResetOnStop`, `zoomLevel`.
- Misc visual: `hideScoreboard`, `smoothSneak`, `customNameTags`, `showOwnNametag=true`, `hideFishingBobber=true`, `fishingRodOverlay`, `fishingRodOverlayColor`, `fishingRodOverlayAlpha`, `elytraPitchHud=false`, `elytraPitchShowYaw=false`, `pingOnNametag=false`, `pingNametagAutoColor=true`, `pingNametagFormat="%dms"`, `pingNametagPosition (enum)`.
- Legacy clean-F3 hide flags: `cleanF3Hide{FPS,Ping,Coordinates,ChunkUpdates,Biome}`, `cleanF3CompactMode`.

### `Combat`
- Attack cooldown: `customAttackCooldownIndicator=true`, `chargedSound=true`, `chargedColor=true`, `chargedColorArgb=-12326533`, `unchargedColorArgb=-5195837`, `cooldownOffset{X=-14,Y=14}`, `cooldownWidth=28`, `cooldownHeight=2`, `cooldownScalePercent=100`, `cooldownVertical=false`, `cooldownOutline=true`, `cooldownOnlyWeapon=false`.
- Totem counter: `totemCounterHud=false`, `totemLabelStyle (enum)`, `totemColorByCount=true`, `totemNametagPops=true`, `totemShowPopCounter=true`, `totemColoredXpBar=false`, `totemAlwaysShowXpBar=false`, `totemShowInTab=false`, `totemSeparator=false`, `totemCounterColors=false`.
- Potion-throw counter: `potionThrowCounterHud=false`, `potionThrowNametagPots=true`, `potionThrowShowInTab=true`, `potionThrowColoredXpBar=false`, `potionThrowAlwaysShowXpBar=true`, `potionThrowSeparator=false`, `potionThrowCounterColors=false`.
- Health indicator: `playerHealthIndicator`, `playerHealthIndicatorInvisible`, `playerHealthIndicatorArmorOnly=false`, `playerHealthIndicatorStyle (enum)`, `playerHealthIndicatorMaxHearts`, `showExactHealthNumber`, `healthOffset{X,Y}`, `healthScalePercent`.

### `Hud`
- Chat: `compactChat=true`, `hideChat=false`, `chatTimestamps=true`, `chatTimestampFormat="HH:mm"`, `compactChatDistance=1`, `compactChatCheckStyle=true`, `betterScreenshotActions=true`.
- Inventory HUD: `inventoryHudEnabled=false`, `inventoryHudX=0`, `inventoryHudY=203`, `inventoryHudScalePercent=65`, `inventoryHudBackground=true`.
- Coordinates HUD: `coordinatesHud=false`, `coordinatesHudX=5`, `coordinatesHudY=200`, `coordinatesHudScalePercent=100`, `coordinatesHudMode (enum)`, `coordsShow{Chunk,Direction,Biome,Dimension,Day,Background}=true`.
- Tab ping: `pingInTab=false`, `pingTabAutoColor=true`, `pingTabFormat="%dms"`, `pingTabColor=-1`.
- Editor: `hudEditMode=false`, `snapToGrid=false`, `gridSize=16`, `snapToCenter=true`, `centerSnapRange=8`.
- Armor HUD: `movableArmorHud=true`, `armorHudX=413`, `armorHudY=271`, `armorHudVertical=true`, `armorHudHotbarStyle=true`, `armorHudShowDurability=true`, `armorHudScalePercent=100`, `armorHudStyle/Side/DurabilityMode (enums)`, `armorHudShowMainHand=false`, `armorHudShowOffhand=false`, `armorHudReserveOffhandSpace=true`, `armorHudWarnings=true`, `armorHudWarningThresholdPercent=20`.
- Potion HUD: `movablePotionHud=true`, `potionHudX=613`, `potionHudY=0`, `potionMaxRows=6`, `potionHudColumns=1`, `potionRowSpacing=28`, `potionShowFlags=true`, `potionTimerCompact=true`, `potionTimerShowAmplifier=true`, `hideVanillaPotionHud=true`, `potionSortMode/potionHudStyle (enums)`, `potionHudScalePercent`.
- FPS/ping overlay: `minimalFpsPingOverlay`, `fpsColorCoded=false`, `minimalOverlay{X,Y}`, `overlayScalePercent`.
- Clean F3: `cleanF3Mode`, `cleanF3Show{FpsPing,Position,Facing,Biome,LookingAt,Light,Chunk,Background,Speed,DayTime,Memory,HeldItem,Dimension,FpsExtremes}`, `cleanF3LabelColor=0xFF7FE08A`, `cleanF3ValueColor=0xFFE6E6E6`, `cleanF3Order (List<String>)`, `cleanF3{X,Y}`, `cleanF3ScalePercent`. Helpers `ensureCleanF3()`, `defaultCleanF3Order()`.
- Hitboxes: `customHitboxes`, `hitbox{Players,Hostile,Passive,Others,Self}`, `hitboxMaxDistance`, `hitboxColor`, `hitboxChangeTargetColor`, `hitboxTargetColor`, `hitboxHurtColorEnabled`, `hitboxHurtColor`, `hitboxHideFireworks`, `hitboxShowInvisible=false`, `hitboxShowInvisibleArmorOnly=false`, `hitboxShowInvisibleEntities=false`, `cleanDebugHitboxes=false`.
- Totem HUD: `totemHud{X,Y}`, `totemHudScalePercent`.
- Reach: `reachDisplay`, `reachHud{X,Y}`, `reachHudScalePercent`, `reachShowTypeTag`, `reachShowEntityName`, `reachTrackNearestPlayer`, `reachMaxSearchDistance`, `reachDecimals`, `reachDisplayTicks`.
- Sprint: `toggleSprintHud`, `toggleSprintHud{X,Y}`, `toggleSprintHudScalePercent`, `sprintDisplayStyle (enum)`, `sprintShowSneaking=true`, `sprintShowSwimming=true`.
- Keystrokes: `keystrokesHud`, `keystrokesShowCps`, `keystrokesHud{X,Y}`, `keystrokesHudScalePercent`, `keystrokesUsePressedColor=true`, `keystrokesPressedColor=-7815081`, `keystrokesPressedTextColor=-1`.
- CPS: `cpsCounterHud`, `cpsCounter{X,Y}`, `cpsCounterScalePercent`, `cpsShowBoth`, `cpsShowRightClick`, `cpsShowBackground`, `cpsRainbow`.
- Global: `globalHudScalePercent`, `globalHudOpacityPercent`.

### `CustomTheme`
`hudBackgroundColor=-14540254`, `hudBackgroundAlpha=49`, `hudBorderColor=-9790395`, `hudBorderThickness=1`, `hudShowBorders=true`, `slotOutlines=true`, `hudTextColor=-1`, `hudAccentColor=-11296965`, `enableShadows=true`, `hudTextBold=false`, `shadowSize=4`, `cornerRadius=7`, `themeAlphaPercent=100`, `keyBackgroundColor=-14799074`, `keyActiveColor=-9724347`, `keyTextColor=-6704999`, `keyActiveTextColor=-1`.

### `Misc`
`enabled=true`, `containerButtons=true`, `noOpGamemodeSwitcher=false`, `discordRpc = DiscordRpc{enabled=true, showUsername/ServerName/Dimension=false}`, `deathCoords=true`, `hideParticlesEnabled=true`, `muteSoundsEnabled=true`, `hideParticles=false`, `mute{Anvil,NoteBlocks,TotemPop,XpOrb}=false`, `commandKeysEnabled=true`, `commandKeys (legacy String[5])`, `commandKeyMacros (CommandKey[8])`, `hiddenParticleIds (List)`, `mutedSoundIds (List)`. Helper `ensureCommandKeys()`.

### Helper types & enums
- `ShieldColorConfig{usableColor,brokenColor,useCustomColors}`; `ToggleWithSlider{enabled,value,min,max}`; `HitColorConfig` (visual pkg).
- `CmdMsg{String text, int delay}`; `CommandKey{List<CmdMsg> messages, String mode (SEND|CYCLE|REPEAT), boolean typeInChat}`.
- Enums: `HurtCamMode`, `PotionSortMode{DURATION_DESC,DURATION_ASC,AMPLIFIER_DESC,AMPLIFIER_ASC,NAME_ASC,NAME_DESC}`, `PotionHudStyle`, `TotemLabelStyle`, `PlayerHealthIndicatorStyle`, `ArmorHudStyle`, `ArmorHudSide`, `ArmorHudDurabilityMode`, `CoordinatesHudMode`, `SprintDisplayStyle`, `PingTextPosition`.

---

## 3. Mixin reference (target → hooked method → injector → effect)

| Mixin | Target | Method(s) | Injector | Effect |
|---|---|---|---|---|
| AbstractClientPlayerEntityMixin | class_742 | method_52810 | @Inject HEAD | skin texture override (cosmetics) |
| CameraMixin | class_4184 | method_19321 | @WrapOperation ×2 (method_5705/5695) | freelook camera angle override |
| ChatInputSuggestorMixin | class_4717 | method_23934 | @Inject HEAD | chat input suggestor tweak |
| ChatScreenMixin | class_408 | method_44056 | @ModifyVariable STORE | chat text handling |
| ChatTimestampMixin | class_338 (ChatHud) | method_44811 | @ModifyVariable HEAD | prepend chat timestamps |
| ClientPlayNetworkHandlerMixin | class_634 | method_11146, method_11148 | @Inject HEAD | session/scoreboard hooks |
| ClientPlayerEntityBlockBreakMixin | class_746 | method_5773 (tick) | @Inject HEAD | block-break related tracking |
| ClientPlayerInteractionManagerMixin | class_636 | method_2918 | @Inject HEAD | interaction hook |
| ClientWorldMixin | class_638 | method_43207 | @Inject HEAD | world add-entity / trails / pearl hook |
| CompactChatMixin | class_338 | method_44811 ×2 | @Inject HEAD | dedupe/compact chat |
| DebugHitboxCleanupMixin | class_12155 (EntityRenderDispatcher) | method_75432 | @Redirect ×4 (class_12180 hitbox builders) | clean debug hitboxes |
| DebugHudMixin | class_340 (DebugHud) | method_1846 | @Inject HEAD cancellable | Clean F3 replacement → `CleanF3Feature.render` |
| EntityMixin | class_1297 | method_5872 | @Inject HEAD | entity nameplate/ping hook |
| EntityNameTagPingMixin | class_897 (EntityRenderer) | method_62426 | @Inject RETURN | ping on entity nametag |
| EntityRenderDispatcherMixin | class_12155 | method_75432, method_23109 | @ModifyVariable STORE, @Redirect (method_5767 isInvisible) | reveal invisible hitboxes/players/entities |
| EntityRendererMixin | class_1007 (PlayerEntityRenderer) | method_62604, method_74935 | @Inject TAIL, @ModifyReturnValue | own nametag / nametag visibility |
| EquipmentLayerRendererMixin | class_10197 | method_64078 | @ModifyExpressionValue ×3 (field_21444), @WrapOperation ×3 | armor damage/trim tint |
| FishingBobberRendererMixin | class_906 | method_62442 | @Inject HEAD | hide fishing bobber |
| FlyingItemEntityRendererMixin | class_953 | method_62548 | @Inject HEAD | thrown-item render tweak |
| FogEnvironmentMixin | class_11401/11402/7283/7284/11403/11398 | method_42591, method_42592 | @Inject TAIL/HEAD | fog environment disable/limit (≥1.21.6) |
| FogRendererMixin | class_758 | method_3211, method_71652 | @ModifyVariable HEAD, @Inject RETURN | fog distance / disable |
| GameMenuScreenMixin | class_433 | method_20543 | @Inject TAIL | add TurtMod button to pause menu |
| GameModeSwitcherKeyMixin | class_309 (Keyboard) | method_1468 (handleDebugKeys) | @Inject HEAD cancellable | open GM switcher without op (F3+F4) |
| GameModeSwitcherMixin | class_5289 | method_28064 | @Redirect ×2 (class_12090.method_75022 perm, class_634.method_52787 send) | bypass perm + send `/gamemode` |
| GameOptionsAccessor | class_315 | @Accessor field_21333 | accessor | perspective access |
| GameRendererMixin | class_757 | method_3196/3199/3188/3198 | @Inject + @WrapOperation | zoom FOV / hurt tilt / render hooks |
| HandledScreenMixin(+Accessor) | class_465 | — / field_2776,2800,2792 | accessor | container button positioning |
| HeldItemScaleMixin | class_759 (HeldItemRenderer) | method_3233 | @Inject HEAD+RETURN | held-item & totem scale/pos/rotation |
| HideParticlesMixin | class_702 (ParticleManager) | method_3056 | @Inject HEAD cancellable | hide all / per-id particles |
| HideScoreboardMixin | class_329 (InGameHud) | method_55803, method_1757 | @Inject HEAD cancellable | hide sidebar scoreboard |
| HitColorMixin | class_4608 (OverlayTexture) | `<init>` | @Inject TAIL | custom hit/damage tint |
| InGameHudMixin | class_329 | method_1746/31977/32598/1735, method_1760 | @Inject + @ModifyArg | HUD render hooks + armor bar arg |
| InGameOverlayRendererMixin | class_4603 | method_23067 | @Redirect | in-water/overlay tweak |
| LightmapTextureManagerMixin | class_765 | method_3313 | @ModifyArg ×6 (Std140 putFloat) | fullbright lightmap |
| LivingEntityAccessor | class_1309 | @Accessor field_6235 (hurtTime) | accessor | hit color timing |
| LivingEntityRenderer(+Accessor)Mixin | class_922 | method_4054, method_4055 | @Inject + @ModifyExpressionValue | health indicator / shield context |
| LivingEntityStatusMixin | class_1309 | method_5711 | @Inject HEAD | totem/status events |
| LowFireOverlayMixin | class_4603 | method_23070 | @Inject HEAD + @ModifyArg (method_46416) | fire overlay offset/hide |
| MouseMixin | class_312 | method_1601/1598/1606 | @Inject + @WrapOperation ×3 | zoom scroll, freelook sens |
| MuteSoundsMixin | class_638 | method_8486, method_8465 | @Inject HEAD cancellable | mute all / per-id sounds |
| PlayerEntityAttackMixin | class_1657 | method_7324 | @Inject | attack/cooldown feedback |
| PlayerEntityMixin | class_1657 | method_5476 (displayName) | @ModifyReturnValue | nametag tweaks |
| PlayerEntityRenderStateMixin | class_10055 | — (fields) | state holder | health/render-state extension |
| PlayerHeldItemFeatureRendererMixin | class_5697 | method_62594 ×2 | @Inject | third-person held item tweaks |
| PlayerListPingInvoker | class_355 | @Invoker method_1923 | invoker | tab ping access |
| PlayerListPingMixin | class_355 | method_1919 | @ModifyConstant + @Redirect | tab ping number/width |
| PlayerTabOverlayMixin | class_355 | method_1918 | @Inject | tab list ping/render |
| PotionHudMixin | class_329 | method_1765 | @Inject HEAD cancellable | hide vanilla potion HUD |
| PotionThrowEntityDataMixin | class_634 | method_11093 | @Inject TAIL | splash-potion tracking data |
| ScreenshotChatActionsMixin | class_338 | method_44811 | @Inject | screenshot toast chat actions |
| ShieldModelRendererMixin | class_10509 | method_65707 | @Inject | shield recolor/size |
| SplashScreenMixin | class_425 | method_25394 | @Inject TAIL | branded loading overlay |
| TextDisplayRendererMixin | class_8138$class_8141 | method_49057 | @ModifyVariable | text-display render tweak |
| TitleScreenMixin | class_442 | method_25426 | @Inject | add TurtMod logo button |
| TitleScreenVisualsMixin | class_442 | method_25394 | @Inject TAIL | title screen branding |
| TotemPopGameRendererMixin | class_759 | method_3198 | @Inject HEAD | totem pop scale |
| TotemPopRoundEndMixin | class_338 | method_44811 | @Inject HEAD | reset pop counters on round end |
| TotemPopScaleMixin | class_4603 | method_70939 | @WrapOperation + @ModifyArgs ×2 | totem pop overlay scale |
| TotemPopXpBarMixin | class_11224 | method_70865 | @Inject RETURN | totem count on XP bar |
| WorldRendererMixin | class_761 | method_22712 | @ModifyVariable + @ModifyArg | block outline recolor |

Non-`mixin/client` accessors: `mixin/OverlayTextureAccessor`, `mixin/SimpleOptionDuck`, `mixin/client/SimpleOptionAccessor`, `mixin/client/ScreenMixinAccessor`.

---

## 4. Class catalog (by package)

**root** — `TurtModClient` (entry/keybinds/commands/tick).

**config/** — `TurtModConfig` (schema), `ConfigManager` (persistence+migrate), `TurtModClientConfigScreen` (module grid, tabs VISUALS/HUD/UTILITY/MISC), `TurtModConfigScreenFactory` (native config-tree builder + per-module pages + `ModuleKind` enum; was `TurtModWalksyConfigScreenFactory`), `TurtModMainMenuScreen`, `RegistryPickerScreen` (particle/sound picker w/ thumbnails), `CommandKeysScreen` + `CommandKeyEditScreen` (macro editor), `CleanF3OrderScreen` (line reorder), `KeybindListenScreen` (rebinder), `ModMenuIntegration`.

**ui/** — `Palette`, `TurtLauncher` (chrome + `drawContentPanel`, `drawChrome` w/ optional sidebar), `TurtUIUtils` (rounded rect/border — cheap O(r) corner-cut, gradients, glow, `drawStage`, backdrop, lerp), `TurtUIButton`, `TurtUICheckbox`, `TurtUILabel`, `TurtUIPanel`, `TurtUIScale`, `TurtUITheme`, `BrandingRenderer`, `TurtLogoButton`, `TurtChat` (gradient chat msgs), `ClientUiTheme`.

**ui/config/** — `TurtNativeConfigScreen` (native settings screen: horizontal tabs, toggles, sliders + click-to-type fields, enum dropdowns, HSB color picker). **ui/config/model/** — `LocalConfig`, `Category`, `OptionGroup`, `Option<T>`, `OptionDescription`, `ConfigColor` (native config model replacing WalksyLib).

**hud/** — `HudPanelsFeature` (armor+potion panels), `CleanF3Feature`, `CommandKeysFeature` (macro scheduler), `CoordinatesHudFeature`, `CpsCounterFeature`, `CustomThemeRenderer`, `DeathCoordsFeature`, `ElytraPitchFeature`, `FpsPingOverlayFeature`, `InventoryHudFeature`, `KeystrokesFeature`, `ReachDisplayFeature`, `ToggleSprintFeature`, `HudEditorScreen` + `HudEditorFeature` (drag/scale/anchors).

**visual/** — `FullbrightFeature`, `FreeLookFeature`, `ZoomFeature`, `HurtCamFeature`, `ScreenEffectsFeature`, `HitColorConfig`.

**combat/** — `HealthNumberFeature`, `PlayerHeartSpriteRenderer`, `TotemCounterFeature`, `TotemPopTracker`, `PotionThrowTracker`.

**chat/** — `BetterChatFeature`, `BetterScreenshotFeature`, `ScreenshotUploadFeature`.

**cosmetics/** — `CosmeticsScreen` (3D skin preview), `CosmeticManager`, `CosmeticProfile`, `skin/SkinLoader`.

**gallery/** — `ScreenshotGalleryScreen` (uniform grid), `ScreenshotViewScreen`.

**kit/** — `KitManager`, `KitSerializer` (SNBT + `deserializeIcons`), `KitIO`, `KitManagerScreen`, `KitPreviewScreen`.

**discord/** — `TurtDiscordRpcService`.

**utils/** — `CPSTracker`, `PingColors`, `ShieldTracker`, `ShieldEntityContext`, `GrayscaleTextureCache`, `ImageClipboardUtils`, `Animation`, `TurtLogger`.

**event/** `OverlayReloadListener`; **extension/** `OverlayRendered`.

---

## 5. Keybinds (categories: General / Visual / HUD / Modules)
`open_config` (N), `freelook_hold` (Right Alt), `zoom`, `health_config`, `health_toggle`, `health_offset_up`, `health_offset_down`, `health_offset_reset`, `command_key_1..8`, and one `toggle.<module>` per module (all unbound by default).

## 6. Commands (`/turtmod …`)
- `cmdkey set <1-8> <text>` / `clear <slot>` / `list` → writes `commandKeyMacros`.
- `kit save|load|delete|preview <name>` / `list`.
- `particle hide|show <id>` / `list` / `clear`.
- `sound mute|unmute <id>` / `list` / `clear`.
- `screenshot`, `uploadlastscreenshot`.

## 7. Notes / gotchas
- Mixins use **intermediary** names verified vs the decompiled 1.21.11 jar; never yarn.
- `class_12090/class_12096` (permission API) exist only ≥1.21.10 → 1.21.9 build needs a Stonecutter guard around `GameModeSwitcherMixin`/`GameModeSwitcherKeyMixin`.
- `DebugHudMixin` gates on `method_72776()` (text-screen toggle) not `method_53536()` so F3+B/G don't trigger Clean F3.
- GSON instantiates configs via Unsafe → field initializers are skipped for loaded JSON; defaults set in constructors + `migrate()` fixups (clean-F3 colors/order, command-key macros).
