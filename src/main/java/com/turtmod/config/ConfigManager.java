package com.turtmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.fabricmc.loader.api.FabricLoader;

public final class ConfigManager {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("turtmod.json");
   private static final DateTimeFormatter BACKUP_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

   private ConfigManager() {
   }

   public static TurtModConfig load() {
      return migrate(loadRaw());
   }

   private static TurtModConfig loadRaw() {
      if (!Files.exists(CONFIG_PATH, new LinkOption[0])) {
         TurtModConfig defaults = loadBundledDefaults();
         save(defaults);
         return defaults;
      } else {
         try {
            String json = Files.readString(CONFIG_PATH);
            TurtModConfig parsed = (TurtModConfig)GSON.fromJson(json, TurtModConfig.class);
            return parsed == null ? recoverWithDefaults() : parsed;
         } catch (JsonSyntaxException | IllegalStateException | IOException var2) {
            return recoverWithDefaults();
         }
      }
   }

   /** Forward-compat fixups applied to any loaded config (defaults included). */
   private static TurtModConfig migrate(TurtModConfig config) {
      if (config != null) {
         if (config.misc != null) {
            config.misc.ensureCommandKeys();
            config.misc.ensurePinned();
         }
         if (config.hud != null) {
            config.hud.ensureCleanF3();
         }
      }
      return config;
   }

   public static void save(TurtModConfig config) {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         Files.writeString(CONFIG_PATH, GSON.toJson(config));
      } catch (IOException var2) {
      }

   }

   private static TurtModConfig recoverWithDefaults() {
      TurtModConfig defaults = loadBundledDefaults();
      backupBrokenConfig();
      save(defaults);
      return defaults;
   }

   private static TurtModConfig loadBundledDefaults() {
      try {
         InputStream stream = ConfigManager.class.getClassLoader().getResourceAsStream("turtmod.default.json");

         TurtModConfig var2;
         label52: {
            try {
               if (stream != null) {
                  TurtModConfig parsed = (TurtModConfig)GSON.fromJson(new InputStreamReader(stream), TurtModConfig.class);
                  if (parsed != null) {
                     var2 = parsed;
                     break label52;
                  }
               }
            } catch (Throwable var4) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var3) {
                     var4.addSuppressed(var3);
                  }
               }

               throw var4;
            }

            if (stream != null) {
               stream.close();
            }

            return new TurtModConfig();
         }

         if (stream != null) {
            stream.close();
         }

         return var2;
      } catch (JsonSyntaxException | IllegalStateException | IOException var5) {
         return new TurtModConfig();
      }
   }

   private static void backupBrokenConfig() {
      if (Files.exists(CONFIG_PATH, new LinkOption[0])) {
         try {
            String stamp = LocalDateTime.now().format(BACKUP_TIME);
            Path backup = CONFIG_PATH.resolveSibling("turtmod-broken-" + stamp + ".json");
            Files.copy(CONFIG_PATH, backup);
         } catch (IOException var2) {
         }

      }
   }

   public static void reset() {
      TurtModConfig defaults = loadBundledDefaults();
      backupBrokenConfig();
      save(defaults);
   }
}
