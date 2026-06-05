package com.turtmod.discord;

import com.google.gson.JsonObject;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.ActivityType;
import com.jagrosh.discordipc.entities.DiscordBuild;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.StatusDisplayType;
import com.jagrosh.discordipc.entities.User;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import com.turtmod.config.TurtModConfig;
import com.turtmod.utils.TurtLogger;
import java.util.Objects;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_642;

public final class TurtDiscordRpcService {
   public static final String DEFAULT_APPLICATION_ID = "1490094633709994034";
   public static final String DEFAULT_LARGE_IMAGE_KEY = "logo";
   public static final String DEFAULT_LARGE_IMAGE_TEXT = "TurtMod";
   private static final int PRESENCE_INTERVAL_TICKS = 40;
   private static final int CONNECT_RETRY_INTERVAL_TICKS = 100;
   private static IPCClient ipcClient;
   private static boolean connectAttemptInProgress = false;
   private static boolean warnedNoDiscord = false;
   private static int connectRetryTicks = 0;
   private static int presenceTicks = 0;
   private static long activityStartedAt = epochSeconds();
   private static String currentApplicationId = "";
   private static String lastActivityKey = "";
   private static String lastDetails = "";
   private static String lastState = "";

   private TurtDiscordRpcService() {
   }

   public static void bootstrap(TurtModConfig config) {
      if (config != null) {
         TurtModConfig.DiscordRpc rpc = config.misc.discordRpc;
         if (config.misc.enabled && rpc.enabled) {
            if (ensureClient("1490094633709994034")) {
               if (!isConnected()) {
                  tryConnect();
               }

            }
         } else {
            shutdown();
         }
      }
   }

   public static void tick(class_310 client, TurtModConfig config) {
      if (client != null && config != null) {
         TurtModConfig.DiscordRpc rpc = config.misc.discordRpc;
         if (config.misc.enabled && rpc.enabled) {
            if (ensureClient("1490094633709994034")) {
               if (!isConnected()) {
                  tryConnect();
               } else {
                  warnedNoDiscord = false;
                  if (++presenceTicks >= 40) {
                     presenceTicks = 0;
                     PresenceSnapshot snapshot = buildSnapshot(client, rpc);
                     if (snapshot != null) {
                        if (!Objects.equals(lastActivityKey, snapshot.activityKey())) {
                           activityStartedAt = epochSeconds();
                        }

                        if (!matchesLastSnapshot(snapshot)) {
                           updatePresence(snapshot);
                           cacheSnapshot(snapshot);
                        }
                     }
                  }
               }
            }
         } else {
            shutdown();
         }
      }
   }

   public static void shutdown() {
      presenceTicks = 0;
      connectRetryTicks = 0;
      connectAttemptInProgress = false;
      currentApplicationId = "";
      lastActivityKey = "";
      lastDetails = "";
      lastState = "";
      if (ipcClient != null) {
         try {
            ipcClient.sendRichPresence((RichPresence)null);
         } catch (Throwable var2) {
         }

         try {
            ipcClient.close();
         } catch (Throwable var1) {
         }
      }

      ipcClient = null;
   }

   private static boolean ensureClient(String applicationId) {
      if (ipcClient != null && Objects.equals(currentApplicationId, applicationId)) {
         return true;
      } else {
         shutdown();

         long clientId;
         try {
            clientId = Long.parseLong(applicationId);
         } catch (NumberFormatException var5) {
            TurtLogger.error("Discord RPC application ID is invalid.");
            return false;
         }

         try {
            IPCClient newClient = new IPCClient(clientId, false, false, true, applicationId);
            newClient.setListener(new TurtDiscordIpcListener());
            ipcClient = newClient;
            currentApplicationId = applicationId;
            activityStartedAt = epochSeconds();
            TurtLogger.info("Discord IPC client created.");
            return true;
         } catch (Throwable t) {
            TurtLogger.error("Failed to create Discord IPC client: " + t.getClass().getSimpleName());
            ipcClient = null;
            currentApplicationId = "";
            return false;
         }
      }
   }

   private static void tryConnect() {
      if (ipcClient != null && !connectAttemptInProgress) {
         if (connectRetryTicks > 0) {
            --connectRetryTicks;
         } else {
            IPCClient clientRef = ipcClient;
            connectAttemptInProgress = true;
            TurtLogger.info("Connecting Discord RPC...");
            Thread thread = new Thread(() -> {
               try {
                  clientRef.connect(new DiscordBuild[]{DiscordBuild.ANY});
               } catch (NoDiscordClientException var6) {
                  if (!warnedNoDiscord) {
                     warnedNoDiscord = true;
                     TurtLogger.warning("Discord RPC could not find an open Discord client.");
                  }
               } catch (Throwable t) {
                  TurtLogger.error("Failed to connect Discord IPC: " + t.getClass().getSimpleName());
               } finally {
                  connectAttemptInProgress = false;
                  connectRetryTicks = 100;
               }

            }, "TurtMod-Discord-IPC");
            thread.setDaemon(true);
            thread.start();
         }
      }
   }

   private static boolean isConnected() {
      return ipcClient != null && ipcClient.getStatus() == PipeStatus.CONNECTED;
   }

   private static void updatePresence(PresenceSnapshot snapshot) {
      if (isConnected()) {
         try {
            String largeKey = blankToNull(snapshot.largeKey()) == null ? "logo" : snapshot.largeKey();
            String largeText = blankToNull(snapshot.largeText()) == null ? "TurtMod" : snapshot.largeText();
            RichPresence.Builder builder = (new RichPresence.Builder())
               .setActivityType(ActivityType.Playing)
               .setStatusDisplayType(StatusDisplayType.Details)
               .setName("TurtMod")
               .setDetails(orEmpty(snapshot.details()))
               .setState(orEmpty(snapshot.state()))
               .setStartTimestamp(activityStartedAt)
               // Large image = the dimension/world art (tooltip = server/world name); small = TurtMod logo.
               .setLargeImageWithTooltip(largeKey, largeText)
               .setSmallImageWithTooltip("logo", "TurtMod")
               .setInstance(true);
            ipcClient.sendRichPresence(builder.build());
         } catch (Throwable t) {
            TurtLogger.error("Failed to update Discord RPC presence: " + t.getClass().getSimpleName());
         }

      }
   }

   private static void sendBootstrapPresence() {
      if (isConnected()) {
         try {
            ipcClient.sendRichPresence((new RichPresence.Builder())
               .setActivityType(ActivityType.Playing)
               .setStatusDisplayType(StatusDisplayType.Details)
               .setName("TurtMod")
               .setDetails("Loading TurtMod")
               .setState("on minecraft " + mcVersion())
               .setStartTimestamp(activityStartedAt)
               .setLargeImageWithTooltip("logo", "TurtMod")
               .setSmallImageWithTooltip("logo", "TurtMod")
               .setInstance(true).build());
            lastActivityKey = "startup";
            lastDetails = "Loading TurtMod";
            lastState = "on minecraft " + mcVersion();
         } catch (Throwable t) {
            TurtLogger.error("Failed to send startup Discord RPC presence: " + t.getClass().getSimpleName());
         }

      }
   }

   private static PresenceSnapshot buildSnapshot(class_310 client, TurtModConfig.DiscordRpc rpc) {
      class_437 screen = client.field_1755;
      String screenName = screen == null ? "" : screen.getClass().getSimpleName();
      String screenClassName = screen == null ? "" : screen.getClass().getName();
      String onVersion = "on minecraft " + mcVersion();

      // In-world: the exact layout the user asked for — "TurtMod" / "Playing <server-or-world>" /
      // "on minecraft <version>", with the large image keyed to the dimension (tooltip = the
      // server/world name) and the small image being the TurtMod logo (set in updatePresence).
      if (client.field_1724 != null && client.field_1687 != null && !(screen != null && screenClassName.startsWith("com.turtmod."))) {
         boolean singleplayer = client.method_1576() != null;
         String scene = sceneName(client, singleplayer);
         String largeKey = dimensionAsset(client);
         // activityKey carries scene + dimension so changing server/world/dimension refreshes the card.
         String activityKey = (singleplayer ? "singleplayer:" : "multiplayer:") + scene + ":" + largeKey;
         return new PresenceSnapshot(activityKey, trim("Playing " + scene), trim(onVersion), largeKey, trim(scene));
      }

      // Menus / config — keep the same name + version line, large image is the TurtMod logo.
      if (screen != null && screenClassName.startsWith("com.turtmod.")) {
         return new PresenceSnapshot("turtmod:config", trim("Editing TurtMod settings"), trim(onVersion), "logo", "TurtMod");
      } else if ("TitleScreen".equals(screenName)) {
         return new PresenceSnapshot("menu:title", trim("Main menu"), trim(onVersion), "logo", "TurtMod");
      } else if ("SelectWorldScreen".equals(screenName)) {
         return new PresenceSnapshot("menu:worlds", trim("Selecting a world"), trim(onVersion), "logo", "TurtMod");
      } else if (!screenName.contains("Multiplayer") && !"AddServerScreen".equals(screenName) && !"DirectConnectScreen".equals(screenName)) {
         if ("ConnectScreen".equals(screenName) || "DownloadingTerrainScreen".equals(screenName)) {
            String joining = sceneName(client, false);
            return new PresenceSnapshot("menu:joining:" + joining, trim("Joining " + joining), trim(onVersion), "logo", "TurtMod");
         }
         return new PresenceSnapshot("menu:generic", trim("In menus"), trim(onVersion), "logo", "TurtMod");
      } else {
         return new PresenceSnapshot("menu:servers", trim("Browsing servers"), trim(onVersion), "logo", "TurtMod");
      }
   }

   /** The running game's Minecraft version, e.g. "1.21.11" (via the Fabric mod metadata). */
   private static String mcVersion() {
      try {
         return net.fabricmc.loader.api.FabricLoader.getInstance()
            .getModContainer("minecraft")
            .map(c -> c.getMetadata().getVersion().getFriendlyString())
            .orElse("");
      } catch (Throwable t) {
         return "";
      }
   }

   /** The "scene" shown after "Playing " — the server name (multiplayer) or the world name (singleplayer). */
   private static String sceneName(class_310 client, boolean singleplayer) {
      if (singleplayer) {
         try {
            if (client.method_1576() != null) {
               String name = blankToNull(client.method_1576().method_27728().method_150());
               if (name != null) {
                  return name;
               }
            }
         } catch (Throwable ignored) {
         }

         return "Singleplayer";
      }

      class_642 server = client.method_1558();
      if (server != null) {
         String name = blankToNull(server.field_3752);
         if (name != null) {
            return name;
         }

         String address = blankToNull(server.field_3761);
         if (address != null) {
            return address;
         }
      }

      return "Multiplayer";
   }

   /** Large-image key from the current dimension. These keys must be uploaded to the Discord app's
    *  Art Assets (overworld / the_nether / the_end); anything else falls back to the TurtMod logo. */
   private static String dimensionAsset(class_310 client) {
      if (client.field_1687 == null) {
         return "logo";
      }

      String dim = client.field_1687.method_27983().method_29177().method_12832();
      return switch (dim) {
         case "overworld" -> "overworld";
         case "the_nether" -> "the_nether";
         case "the_end" -> "the_end";
         default -> "logo";
      };
   }

   private static boolean matchesLastSnapshot(PresenceSnapshot snapshot) {
      return Objects.equals(lastActivityKey, snapshot.activityKey()) && Objects.equals(lastDetails, snapshot.details()) && Objects.equals(lastState, snapshot.state());
   }

   private static void cacheSnapshot(PresenceSnapshot snapshot) {
      lastActivityKey = snapshot.activityKey();
      lastDetails = snapshot.details();
      lastState = snapshot.state();
   }

   private static String trim(String value) {
      String trimmed = blankToNull(value);
      if (trimmed == null) {
         return null;
      } else {
         return trimmed.length() <= 128 ? trimmed : trimmed.substring(0, 128);
      }
   }

   private static String blankToNull(String value) {
      if (value == null) {
         return null;
      } else {
         String trimmed = value.trim();
         return trimmed.isEmpty() ? null : trimmed;
      }
   }

   private static String orEmpty(String value) {
      return value == null ? "" : value;
   }

   private static long epochSeconds() {
      return System.currentTimeMillis() / 1000L;
   }

   private static record PresenceSnapshot(String activityKey, String details, String state, String largeKey, String largeText) {
   }

   private static final class TurtDiscordIpcListener implements IPCListener {
      public void onPacketSent(IPCClient client, Packet packet) {
      }

      public void onPacketReceived(IPCClient client, Packet packet) {
      }

      public void onActivityJoin(IPCClient client, String secret) {
      }

      public void onActivitySpectate(IPCClient client, String secret) {
      }

      public void onActivityJoinRequest(IPCClient client, String secret, User user) {
      }

      public void onReady(IPCClient client) {
         TurtDiscordRpcService.presenceTicks = 40;
         TurtDiscordRpcService.sendBootstrapPresence();
         User currentUser = client.getCurrentUser();
         if (currentUser != null) {
            TurtLogger.success("Discord RPC connected as " + currentUser.getEffectiveName() + ".");
         } else {
            TurtLogger.success("Discord RPC connected.");
         }

      }

      public void onClose(IPCClient client, JsonObject json) {
         TurtDiscordRpcService.connectRetryTicks = 100;
      }

      public void onDisconnect(IPCClient client, Throwable t) {
         TurtDiscordRpcService.connectRetryTicks = 100;
         if (t != null) {
            TurtLogger.warning("Discord RPC disconnected: " + t.getClass().getSimpleName());
         }

      }
   }
}
