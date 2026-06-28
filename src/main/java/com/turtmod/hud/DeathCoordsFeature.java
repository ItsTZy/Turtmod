package com.turtmod.hud;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.kit.KitPreviewScreen;
import com.turtmod.kit.KitSerializer;
import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtChat;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import java.util.Locale;
import net.minecraft.class_124;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_4185;

/**
 * Death coordinates: on death, posts a branded click-to-copy chat line (with dimension) and shows a
 * compact panel + action buttons (copy / respawn&tp / view items) on the death screen. The inventory
 * the player had at death is snapshotted just before death so it can be previewed.
 */
public final class DeathCoordsFeature {
   private static boolean wasDead = false;
   private static boolean haveDeath = false;
   private static double deathX;
   private static double deathY;
   private static double deathZ;
   private static String deathDimId = "";    // e.g. "minecraft:overworld"
   private static String deathInvData = "";   // SNBT of inventory at death

   private static String liveInvSnapshot = ""; // rolling snapshot while alive (~every 10 ticks)
   private static int snapTimer = 0;

   private static boolean pendingTp = false;
   private static int pendingTpDelay = 0;

   private DeathCoordsFeature() {
   }

   public static void tick(class_310 client, TurtModConfig config) {
      // Run a queued "teleport to death location" once we're back in the world after respawning.
      if (pendingTp) {
         if (client.field_1724 != null && client.field_1724.method_5805() && client.field_1724.method_6032() > 0.0F) {
            if (--pendingTpDelay <= 0) {
               sendTpCommand(client);
               pendingTp = false;
            }
         }
         return;
      }

      if (client.field_1724 == null) {
         wasDead = false;
         return;
      }
      boolean alive = client.field_1724.method_5805() && client.field_1724.method_6032() > 0.0F;
      if (alive) {
         // Keep a recent snapshot of the inventory so we can show what the player had at death.
         if (++snapTimer >= 10) {
            snapTimer = 0;
            try {
               liveInvSnapshot = KitSerializer.serialize(client.field_1724.method_31548());
            } catch (Throwable ignored) {
            }
         }
      }
      boolean dead = !alive;
      if (dead && !wasDead && config != null && config.misc.enabled && config.misc.deathCoords) {
         deathX = client.field_1724.method_23317();
         deathY = client.field_1724.method_23318();
         deathZ = client.field_1724.method_23321();
         deathDimId = client.field_1724.method_73183().method_27983().method_29177().toString();
         // Prefer a fresh snapshot if items are still present, else the last live snapshot.
         String now = "";
         try {
            now = KitSerializer.serialize(client.field_1724.method_31548());
         } catch (Throwable ignored) {
         }
         deathInvData = hasItems(now) ? now : liveInvSnapshot;
         haveDeath = true;
         postDeathChat(client);
      }
      wasDead = dead;
   }

   private static boolean hasItems(String snbt) {
      return snbt != null && snbt.contains("\"id\"") || snbt != null && snbt.contains("id:");
   }

   private static String dimName() {
      String path = deathDimId.contains(":") ? deathDimId.substring(deathDimId.indexOf(':') + 1) : deathDimId;
      return switch (path) {
         case "overworld" -> "Overworld";
         case "the_nether" -> "Nether";
         case "the_end" -> "The End";
         default -> path.isEmpty() ? "?" : path;
      };
   }

   private static void postDeathChat(class_310 client) {
      String coords = String.format(Locale.ROOT, "%.0f %.0f %.0f", deathX, deathY, deathZ);
      class_2561 clickable = class_2561.method_43470(coords).method_27694(s -> s
         .method_10977(class_124.field_1060)
         .method_10958(new class_2558.class_10606(coords))
         .method_10949(new net.minecraft.class_2568.class_10613(class_2561.method_43470("Click to copy"))));
      class_2561 msg = TurtChat.prefix()
         .method_10852(class_2561.method_43470("Died at ").method_27692(class_124.field_1080))
         .method_10852(clickable)
         .method_10852(class_2561.method_43470(" in " + dimName()).method_27692(class_124.field_1080));
      client.field_1724.method_7353(msg, false);
   }

   // ── Death-screen overlay (compact panel, between the score and the Respawn button) ───────────
   public static void render(class_332 ctx, class_310 client, int screenWidth, int screenHeight) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg == null || !cfg.misc.enabled || !cfg.misc.deathCoords || !haveDeath || client.field_1772 == null) {
         return;
      }
      class_327 font = client.field_1772;
      String text = String.format(Locale.ROOT, "☠ %.0f, %.0f, %.0f  ·  %s", deathX, deathY, deathZ, dimName());
      int w = font.method_1727(text) + 16;
      int x = (screenWidth - w) / 2;
      int y = screenHeight / 4 + 30; // below the score (y=100), above the Respawn button (h/4+72)
      TurtUIUtils.drawRoundedRect(ctx, x, y, w, 15, 4, new Color(8, 12, 16, 200));
      TurtUIUtils.drawBorder(ctx, x, y, w, 15, Palette.GREEN);
      ctx.method_25300(font, text, screenWidth / 2, y + 4, Palette.GREEN.getRGB());
   }

   /** Add the death-screen action buttons (called from TurtModClient on the death screen). */
   public static void addDeathScreenButtons(class_310 client, class_437 screen) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg == null || !cfg.misc.enabled || !cfg.misc.deathCoords || !haveDeath) {
         return;
      }
      int w = 96;
      int gap = 4;
      int totalW = w * 3 + gap * 2;
      int x = (screen.field_22789 - totalW) / 2;
      int y = screen.field_22790 / 4 + 120; // below the vanilla Respawn / Title Screen buttons

      java.util.List<net.minecraft.class_339> buttons = net.fabricmc.fabric.api.client.screen.v1.Screens.getButtons(screen);
      buttons.add(class_4185.method_46430(class_2561.method_43470("Copy Coords"), b -> copyCoords(client))
         .method_46434(x, y, w, 20).method_46431());
      buttons.add(class_4185.method_46430(class_2561.method_43470("Respawn + TP"), b -> respawnAndTp(client))
         .method_46434(x + w + gap, y, w, 20).method_46431());
      buttons.add(class_4185.method_46430(class_2561.method_43470("View Items"), b -> {
         client.method_1507(new KitPreviewScreen(screen, "Items at Death", deathInvData, null));
      }).method_46434(x + (w + gap) * 2, y, w, 20).method_46431());
   }

   private static void copyCoords(class_310 client) {
      if (client.field_1774 != null) {
         client.field_1774.method_1455(String.format(Locale.ROOT, "%.0f %.0f %.0f", deathX, deathY, deathZ));
      }
   }

   private static void respawnAndTp(class_310 client) {
      if (client.field_1724 != null) {
         client.field_1724.method_7331(); // request respawn
      }
      pendingTp = true;
      pendingTpDelay = 10;
      client.method_1507(null); // close the death screen
   }

   private static void sendTpCommand(class_310 client) {
      if (client.field_1724 == null || client.field_1724.field_3944 == null) {
         return;
      }
      // /execute in <dim> run tp @s x y z — handles cross-dimension. Needs /tp permission.
      String cmd = String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f", deathDimId, deathX, deathY, deathZ);
      client.field_1724.field_3944.method_45730(cmd);
   }
}
