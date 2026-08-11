package com.turtmod.ui;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_332;

/**
 * Tiny turtle-themed pixel-art icons drawn in code (no texture assets) — one per module, each meant to
 * DESCRIBE what its module does. Every icon is a 12x12 grid of single chars (see {@link #COLORS}); '.' is
 * transparent. Drawn as solid cells via {@code method_25294}, so they stay crisp at any GUI scale and
 * re-theme in one place. Unmapped modules fall back to the turtle so nothing is ever blank.
 */
public final class TurtIcons {
   private TurtIcons() {
   }

   // Shared palette (ARGB).
   private static final Map<Character, Integer> COLORS = new HashMap<>();
   static {
      COLORS.put('o', 0xFF17251C); // dark outline
      COLORS.put('g', 0xFF4FB477); // turtle green
      COLORS.put('d', 0xFF2E7D50); // dark green
      COLORS.put('l', 0xFF89E3A8); // light green
      COLORS.put('s', 0xFFEBCD8C); // shell cream
      COLORS.put('S', 0xFFCBA96A); // shell dark
      COLORS.put('p', 0xFFFF8FB0); // pink
      COLORS.put('w', 0xFFF6F8F5); // white
      COLORS.put('y', 0xFFFFD24A); // yellow
      COLORS.put('r', 0xFFE05A6C); // red
      COLORS.put('b', 0xFF79B0E6); // blue
      COLORS.put('n', 0xFF9AA4B0); // grey metal
      COLORS.put('k', 0xFF2B2F37); // dark grey
      COLORS.put('c', 0xFFCFE8FF); // glass
   }

   // ── Icons (each row EXACTLY 12 chars) ────────────────────────────────────────

   private static final String[] TURTLE = {
      "....oooo....",
      "...ogssgo...",
      "..oooooooo..",
      ".ogsgsgsgo..",
      ".ogdglgdgo..",
      ".ogsgpgsgo..",
      ".ogdglgdgo..",
      ".ogsgsgsgo..",
      "..oooooooo..",
      "..o.o..o.o..",
      "..o.o..o.o..",
      "............",
   };

   private static final String[] KEY = {
      "............",
      "...oooooo...",
      "..owwwwwwo..",
      "..og.gg.go..",
      "..og.gg.go..",
      "..og.gg.go..",
      "..og.gg.go..",
      "..oggggggo..",
      "..oggggggo..",
      "...oooooo...",
      "....oooo....",
      "............",
   };

   private static final String[] BARS = {
      "............",
      "............",
      "..........gg",
      "..........gg",
      ".......gg.gg",
      ".......gg.gg",
      "....gg.gg.gg",
      "....gg.gg.gg",
      ".gg.gg.gg.gg",
      ".gg.gg.gg.gg",
      ".gg.gg.gg.gg",
      "............",
   };

   private static final String[] MOUSE = {
      "....oooo....",
      "...owwwwo...",
      "..owwppwwo..",
      "..owwppwwo..",
      "..owggggwo..",
      ".owggggggwo.",
      ".owggggggwo.",
      ".owggggggwo.",
      ".owggggggwo.",
      "..owggggwo..",
      "...oooooo...",
      "............",
   };

   private static final String[] HEART = {
      "............",
      "..rr..rr....",
      ".rrrrrrrr...",
      ".rrwrrrrr...",
      ".rrrrrrrr...",
      "..rrrrrr....",
      "..rrrrrr....",
      "...rrrr.....",
      "....rr......",
      "............",
      "............",
      "............",
   };

   private static final String[] SUN = {
      "............",
      "....y..y....",
      "..y.yyyy.y..",
      "...yyyyyy...",
      "..yyywwyyy..",
      "y.yyywwyyy.y",
      "..yyywwyyy..",
      "...yyyyyy...",
      "..y.yyyy.y..",
      "....y..y....",
      "............",
      "............",
   };

   private static final String[] CAMERA = {
      "............",
      "....ooo.....",
      "...owwwo....",
      ".oooooooooo.",
      ".onnnnnnnno.",
      ".onoooooono.",
      ".onocccoono.",
      ".onocwcoono.",
      ".onocccoono.",
      ".onoooooono.",
      ".oooooooooo.",
      "............",
   };

   private static final String[] BOTTLE = {
      ".....oo.....",
      ".....oo.....",
      "....owwo....",
      "....owwo....",
      "...oooooo...",
      "..oppppppo..",
      "..oppwpppo..",
      "..oppppppo..",
      "..oppppppo..",
      "..oppppppo..",
      "...oooooo...",
      "............",
   };

   private static final String[] MAG = {
      "..ooooo.....",
      ".occccco....",
      "occ.w.cco...",
      "occw.wcco...",
      "occ.w.cco...",
      ".occccco....",
      "..ooooogo...",
      ".......ogo..",
      "........ogo.",
      ".........oo.",
      "............",
      "............",
   };

   private static final String[] ARMOR = {
      "............",
      ".oo....oo...",
      "ossooooosso.",
      "ossssssssso.",
      "osssnnnssso.",
      "ossnwwwnsso.",
      "osssnnnssso.",
      "ossssssssso.",
      ".ossssssso..",
      "..osssso....",
      "...oooo.....",
      "............",
   };

   private static final String[] TARGET = {
      "....oooo....",
      "..oorrrroo..",
      ".orrrrrrrro.",
      ".orwwwwwwro.",
      ".orwrrrrwro.",
      ".orwrwwrwro.",
      ".orwrwwrwro.",
      ".orwrrrrwro.",
      ".orwwwwwwro.",
      ".orrrrrrrro.",
      "..oorrrroo..",
      "....oooo....",
   };

   private static final String[] HITBOX = {
      "..oooooooo..",
      "..o......o..",
      "..o.oooo.o..",
      "..o.o..o.o..",
      "..o.o..o.o..",
      "..o.o..o.o..",
      "..o.o..o.o..",
      "..o.oooo.o..",
      "..o......o..",
      "..oooooooo..",
      "............",
      "............",
   };

   private static final String[] CHEST = {
      "............",
      ".oooooooooo.",
      ".osssssssso.",
      ".osSSSSSSso.",
      ".oooowwoooo.",
      ".osookkooso.",
      ".ossokkosso.",
      ".osssssssso.",
      ".osssssssso.",
      ".oooooooooo.",
      "............",
      "............",
   };

   private static final String[] PIN = {
      "....oooo....",
      "..ooggggoo..",
      ".oggwwwwggo.",
      ".oggwppwggo.",
      ".oggwwwwggo.",
      ".oggggggggo.",
      "..oggggggo..",
      "...oggggo...",
      "....oggo....",
      ".....oo.....",
      "............",
      "............",
   };

   private static final String[] FLAME = {
      ".....o......",
      "....oro.....",
      "...orro.....",
      "...oyro.....",
      "..oyyrro....",
      "..oyywyro...",
      ".oyywwyro...",
      ".oyywwwyro..",
      ".oyywwwyro..",
      ".ooyyyyoo...",
      "..oooooo....",
      "............",
   };

   private static final String[] CLOUD = {
      "............",
      "............",
      "....oooo....",
      "..oowwwwoo..",
      ".owwwwwwwwo.",
      "owwwwwwwwwwo",
      "owwwwwwwwwwo",
      ".oooooooooo.",
      "............",
      "............",
      "............",
      "............",
   };

   private static final String[] SHIELD = {
      "..oooooooo..",
      ".obbbbbbbbo.",
      ".obwbbbbwbo.",
      ".obbwbbwbbo.",
      ".obbbwwbbbo.",
      ".obbwbbwbbo.",
      ".obwbbbbwbo.",
      "..obbbbbbo..",
      "...obbbbo...",
      "....obbo....",
      ".....oo.....",
      "............",
   };

   private static final String[] EYE = {
      "............",
      "............",
      "...oooooo...",
      ".oowwwwwwoo.",
      "owwwgggwwwwo",
      "owwgkkkgwwwo",
      "owwwgggwwwwo",
      ".oowwwwwwoo.",
      "...oooooo...",
      "............",
      "............",
      "............",
   };

   private static final String[] LINES = {
      "............",
      ".oooooooooo.",
      ".ogggggggwo.",
      ".oooooooooo.",
      ".owwwwwgggo.",
      ".oooooooooo.",
      ".ogggwwwwwo.",
      ".oooooooooo.",
      ".owwgggggwo.",
      ".oooooooooo.",
      "............",
      "............",
   };

   private static final String[] BELL = {
      ".....oo.....",
      "....oppo....",
      "...oyyyyo...",
      "..oyyyyyyo..",
      "..oyyyyyyo..",
      ".oyyyyyyyyo.",
      ".oyyyyyyyyo.",
      "oyyyyyyyyyyo",
      "oooooooooooo",
      ".....oo.....",
      "....oppo....",
      "............",
   };

   private static final String[] PALETTE = {
      "...oooooo...",
      "..oppgbboo..",
      ".oprrggbbwo.",
      ".opryybbwwo.",
      ".opyygggwwo.",
      ".oowwwwwwoo.",
      "...oowwoo...",
      "....oooo....",
      "............",
      "............",
      "............",
      "............",
   };

   private static final String[] SWORD = {
      ".........oo.",
      "........owwo",
      ".......owwo.",
      "......owwo..",
      ".....owwo...",
      "....owwo....",
      "..o.owo.....",
      ".oyo.o......",
      "oyyyo.......",
      ".oyo........",
      "..o.........",
      "............",
   };

   private static final String[] BOOT = {
      "............",
      "...oo.......",
      "..oggo......",
      "..oggo......",
      "..oggo......",
      "..oggo......",
      "..ogggoooo..",
      "..ogggggggo.",
      "..ogggggggo.",
      "..ooooooooo.",
      "............",
      "............",
   };

   private static final String[] SKULL = {
      "...oooooo...",
      "..owwwwwwo..",
      ".owwwwwwwwo.",
      ".owkkwwkkwo.",
      ".owkkwwkkwo.",
      ".owwwoowwwo.",
      ".owwwwwwwwo.",
      "..owowowowo.",
      "..oo.oo.oo..",
      "............",
      "............",
      "............",
   };

   private static final String[] GEAR = {
      "....oooo....",
      "..o.o..o.o..",
      ".ooowwwwooo.",
      ".owwggggwwo.",
      "oowg.oo.gwoo",
      "o.wg.oo.gw.o",
      "oowg.oo.gwoo",
      ".owwggggwwo.",
      ".ooowwwwooo.",
      "..o.o..o.o..",
      "....oooo....",
      "............",
   };

   private static final String[] LAYOUT = {
      "............",
      ".oooooooooo.",
      ".oggggogggo.",
      ".oggggogggo.",
      ".oggggogggo.",
      ".oooooooooo.",
      ".oggggogggo.",
      ".oggggogggo.",
      ".oggggogggo.",
      ".oooooooooo.",
      "............",
      "............",
   };

   private static final String[] PERSON = {
      "....oooo....",
      "...owwwwo...",
      "...owwwwo...",
      "....oooo....",
      "..oooooooo..",
      ".oggggggggo.",
      ".oggggggggo.",
      ".oggggggggo.",
      ".oggggggggo.",
      ".oo.....oo..",
      "............",
      "............",
   };

   private static final String[] ARROW = {
      "............",
      "............",
      "......ll....",
      ".....ll.....",
      "....ll......",
      "...ll.......",
      "...ll.......",
      "....ll......",
      ".....ll.....",
      "......ll....",
      "............",
      "............",
   };

   private static final String[] DOTS = {
      "............",
      "............",
      "............",
      "............",
      "............",
      "..gg.gg.gg..",
      "..gg.gg.gg..",
      "............",
      "............",
      "............",
      "............",
      "............",
   };

   private static final String[] DISCORD = {
      "............",
      ".oooooooooo.",
      ".obbbbbbbbo.",
      ".obwbbbbwbo.",
      ".obbbbbbbbo.",
      ".obwwbbwwbo.",
      ".obbbbbbbbo.",
      ".ooooobooo..",
      "......o.....",
      ".....o......",
      "............",
      "............",
   };

   private static final String[] GAMEPAD = {
      "............",
      "............",
      "............",
      ".oooooooooo.",
      "owwgwooprwwo",
      "owgggoowprwo",
      "owwgwooprwwo",
      "owwwwooowwwo",
      ".oooooooooo.",
      "............",
      "............",
      "............",
   };

   private static final String[] RULER = {
      "............",
      "............",
      ".oooooooooo.",
      ".oyyyyyyyyo.",
      ".oywoyoyoyo.",
      ".oyoyoyoyoy.",
      ".oyyyyyyyyo.",
      ".oooooooooo.",
      "............",
      "............",
      "............",
      "............",
   };

   private static final String[] MUTE = {
      "............",
      "......o.....",
      ".....oo..r.r",
      "..oooggo.rr.",
      ".ogggggo..r.",
      ".ogggggo.rr.",
      ".ogggggo.r.r",
      "..oooggo....",
      ".....oo.....",
      "......o.....",
      "............",
      "............",
   };

   private static final String[] SPARKLE = {
      "...p........",
      "..ppp.......",
      "...p...w....",
      ".......www..",
      "........w...",
      "..w.........",
      ".www...p....",
      "..w...ppp...",
      "...p...p....",
      "..ppp.......",
      "...p........",
      "............",
   };

   private static final String[] TOTEM = {
      "....oooo....",
      "...oyggyo...",
      "...oykkyo...",
      "...oyggyo...",
      "..ooyyyyoo..",
      ".oy.oyyo.yo.",
      "..o.oyyo.o..",
      "....oyyo....",
      "....oyyo....",
      "....oyyo....",
      "....oooo....",
      "............",
   };

   private static final String[] HOOK = {
      "....oo......",
      "....go......",
      "....go......",
      "....go......",
      "....go......",
      "....go......",
      "...ogo......",
      "...gog......",
      "..go.og.....",
      "..og.go.....",
      "...ogo......",
      "............",
   };

   private static final String[] CUBE = {
      "............",
      "...oooooo...",
      "..oggggggo..",
      ".oggggggggo.",
      ".oggggggggo.",
      ".oggggggggo.",
      ".oggggggggo.",
      ".oggggggggo.",
      ".oggggggggo.",
      "..oooooooo..",
      "............",
      "............",
   };

   private static final String[] CHAT = {
      "............",
      ".oooooooooo.",
      ".owwwwwwwwo.",
      ".owgggggwwo.",
      ".owwwwwwwwo.",
      ".owggggwwwo.",
      ".owwwwwwwwo.",
      ".oooooboooo.",
      "......o.....",
      ".....o......",
      "............",
      "............",
   };

   private static final String[] KEYBOARD = {
      "............",
      "............",
      ".oooooooooo.",
      ".owowowowoo.",
      ".oooooooooo.",
      ".owowowowoo.",
      ".oooooooooo.",
      ".oowwwwwooo.",
      ".oooooooooo.",
      "............",
      "............",
      "............",
   };

   private static final String[] SIGNAL = {
      "............",
      "...oooooo...",
      "..o......o..",
      ".o..oooo..o.",
      "...o....o...",
      "..o.oooo.o..",
      "....o..o....",
      ".....oo.....",
      ".....ll.....",
      "............",
      "............",
      "............",
   };

   private static final String[] BUTTON = {
      "............",
      "............",
      ".oooooooooo.",
      "owwwwwwwwwwo",
      "oggggggggggo",
      "oggggggggggo",
      "oddddddddddo",
      ".oooooooooo.",
      "............",
      "............",
      "............",
      "............",
   };

   private static final String[] WINGS = {
      "............",
      ".oo......oo.",
      ".owo....owo.",
      ".owwo..owwo.",
      ".owgwoowgwo.",
      ".owggwwggwo.",
      ".owggwwggwo.",
      ".owgwoowgwo.",
      ".owo....owo.",
      ".oo......oo.",
      "............",
      "............",
   };

   private static final String[] COPY = {
      "............",
      "..oooooo....",
      "..owwwwo....",
      "..owwwwooooo",
      "..owwwwowwwo",
      "..ooooowwwwo",
      "....owwwwwwo",
      "....owwwwwwo",
      "....owwwwwwo",
      "....oooooooo",
      "............",
      "............",
   };

   private static final String[] TRASH = {
      "............",
      "...oooooo...",
      "..oooooooo..",
      "...o....o...",
      "..oooooooo..",
      "..owgwgwgo..",
      "..owgwgwgo..",
      "..owgwgwgo..",
      "..owgwgwgo..",
      "..oooooooo..",
      "............",
      "............",
   };

   private static final String[] FOLDER = {
      "............",
      "............",
      ".ooooo......",
      ".owwwooooo..",
      ".owwwwwwwwo.",
      ".oyyyyyyyyo.",
      ".oyyyyyyyyo.",
      ".oyyyyyyyyo.",
      ".oyyyyyyyyo.",
      ".oooooooooo.",
      "............",
      "............",
   };

   private static final String[] EXPORT = {
      "............",
      "....oooooo..",
      "....o..wwo..",
      "....o.wwoo..",
      "....owwoo...",
      ".oowwooo....",
      ".owwoo......",
      ".owoo.......",
      ".ooo........",
      "............",
      "............",
      "............",
   };

   private static final String[] PENCIL = {
      "..........o.",
      ".........owo",
      "........owyo",
      ".......owyo.",
      "......owyo..",
      ".....owyo...",
      "....owyo....",
      "...owyo.....",
      "..oooo......",
      ".oygo.......",
      "..o.........",
      "............",
   };

   private static final String[] ARROWR = {
      "............",
      "............",
      "....ll......",
      ".....ll.....",
      "......ll....",
      ".......ll...",
      ".......ll...",
      "......ll....",
      ".....ll.....",
      "....ll......",
      "............",
      "............",
   };

   private static final String[] PLUS = {
      "............",
      ".....oo.....",
      ".....gg.....",
      ".....gg.....",
      "..ooggggoo..",
      "..gggggggg..",
      "..gggggggg..",
      "..ooggggoo..",
      ".....gg.....",
      ".....gg.....",
      "............",
      "............",
   };

   private static final String[] CHECK = {
      "............",
      "............",
      "..........ll",
      ".........llo",
      "........llo.",
      "..o....llo..",
      "..lo..llo...",
      "..llolll....",
      "...lll......",
      "....l.......",
      "............",
      "............",
   };

   // ── Registry + module mapping ────────────────────────────────────────────────

   private static final Map<String, String[]> MODULES = new HashMap<>();
   static {
      MODULES.put("Keystrokes", KEY);
      MODULES.put("FPS/Ping", BARS);
      MODULES.put("Ping Display", SIGNAL);
      MODULES.put("CPS Counter", MOUSE);
      MODULES.put("Health Indicator", HEART);
      MODULES.put("Fullbright", SUN);
      MODULES.put("Screenshot Tools", CAMERA);
      MODULES.put("Hurt Cam", CAMERA);
      MODULES.put("Potion HUD", BOTTLE);
      MODULES.put("Zoom", MAG);
      MODULES.put("Armor HUD", ARMOR);
      MODULES.put("Hit Color", TARGET);
      MODULES.put("Reach Display", RULER);
      MODULES.put("Hitboxes", HITBOX);
      MODULES.put("Inventory HUD", CHEST);
      MODULES.put("Kit Loader", CHEST);
      MODULES.put("Coordinates", PIN);
      MODULES.put("Death Coords", SKULL);
      MODULES.put("Low Fire", FLAME);
      MODULES.put("Fog Tweaks", CLOUD);
      MODULES.put("Clear View", EYE);
      MODULES.put("Overlays", CLOUD);
      MODULES.put("Shield Tweaks", SHIELD);
      MODULES.put("Freelook", EYE);
      MODULES.put("Own Nametag", CHAT);
      MODULES.put("Clean F3", LINES);
      MODULES.put("Scoreboard Tweaks", LINES);
      MODULES.put("Chat Tweaks", CHAT);
      MODULES.put("Command Keys", KEYBOARD);
      MODULES.put("Module Notifications", BELL);
      MODULES.put("Theme Settings", PALETTE);
      MODULES.put("Block Outline", CUBE);
      MODULES.put("Held Item Tweaks", SWORD);
      MODULES.put("Sprint Display", BOOT);
      MODULES.put("Elytra Pitch HUD", WINGS);
      MODULES.put("Discord RPC", DISCORD);
      MODULES.put("Gamemode Switcher", GAMEPAD);
      MODULES.put("Container Buttons", BUTTON);
      MODULES.put("Fishing Line", HOOK);
      MODULES.put("Mute Sounds", MUTE);
      MODULES.put("Hide Particles", SPARKLE);
      MODULES.put("Totem Tweaks", TOTEM);
      MODULES.put("Enable TurtMod", TURTLE);
   }

   /** Icon for a module display name, or the turtle fallback so nothing is ever blank. */
   public static String[] forModule(String displayName) {
      String[] icon = displayName == null ? null : MODULES.get(displayName);
      return icon != null ? icon : TURTLE;
   }

   // Named icons for hub nav + category tabs.
   public static String[] turtle()    { return TURTLE; }
   public static String[] camera()    { return CAMERA; }
   public static String[] gear()      { return GEAR; }
   public static String[] layout()    { return LAYOUT; }
   public static String[] person()    { return PERSON; }
   public static String[] arrowBack() { return ARROW; }
   public static String[] eye()       { return EYE; }
   public static String[] dots()      { return DOTS; }
   public static String[] wrench()    { return GEAR; }
   public static String[] copy()      { return COPY; }
   public static String[] trash()     { return TRASH; }
   public static String[] folder()    { return FOLDER; }
   public static String[] export()    { return EXPORT; }
   public static String[] pencil()    { return PENCIL; }
   public static String[] arrowFwd()  { return ARROWR; }
   public static String[] person2()   { return PERSON; }
   public static String[] plus()      { return PLUS; }
   public static String[] check()     { return CHECK; }

   // ── Drawing ───────────────────────────────────────────────────────────────────

   /** Draw a 12x12 grid so it fits a {@code box}-px square at (x,y), centred, crisp (integer cells). */
   public static void drawFit(class_332 ctx, String[] grid, int x, int y, int box) {
      if (grid == null || grid.length == 0) {
         return;
      }
      int rows = grid.length;
      int cell = Math.max(1, box / rows);
      int span = cell * rows;
      int ox = x + (box - span) / 2;
      int oy = y + (box - span) / 2;
      for (int r = 0; r < rows; r++) {
         String row = grid[r];
         for (int cIdx = 0; cIdx < row.length(); cIdx++) {
            Integer color = COLORS.get(row.charAt(cIdx));
            if (color != null) {
               int px = ox + cIdx * cell;
               int py = oy + r * cell;
               ctx.method_25294(px, py, px + cell, py + cell, color);
            }
         }
      }
   }
}
