package com.turtmod.kit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.loader.api.FabricLoader;

/**
 * On-disk storage for client kits (ported from ClientKits). One SNBT file per kit under
 * {@code config/turtmod/kits/}; deletes are moved to a {@code deleted/} sub-folder rather than
 * removed, so a mistaken delete is recoverable.
 */
public final class KitIO {
   private static final Path ROOT_DIR = FabricLoader.getInstance().getConfigDir().resolve("turtmod").resolve("kits");
   private static final Path TRASH_DIR = ROOT_DIR.resolve("deleted");
   private static final String EXT = ".json";

   private KitIO() {
   }

   public static void init() {
      try {
         Files.createDirectories(ROOT_DIR);
         Files.createDirectories(TRASH_DIR);
      } catch (IOException e) {
         System.err.println("[TurtMod] Failed to create kit directories: " + e.getMessage());
      }
   }

   /** Sanitised file name — strips path separators so a kit name can't escape the kits folder. */
   private static String safe(String name) {
      return name.replaceAll("[^a-zA-Z0-9_.-]", "_");
   }

   public static void save(String name, String data) throws IOException {
      Files.createDirectories(ROOT_DIR);
      Files.writeString(ROOT_DIR.resolve(safe(name) + EXT), data);
   }

   public static String load(String name) {
      try {
         return Files.readString(ROOT_DIR.resolve(safe(name) + EXT));
      } catch (IOException e) {
         return "";
      }
   }

   public static void moveToTrash(String name) throws IOException {
      Path source = ROOT_DIR.resolve(safe(name) + EXT);
      if (Files.exists(source)) {
         Files.createDirectories(TRASH_DIR);
         Files.move(source, TRASH_DIR.resolve(safe(name) + EXT), StandardCopyOption.REPLACE_EXISTING);
      }
   }

   public static List<String> listKits() {
      try (var stream = Files.list(ROOT_DIR)) {
         return stream.filter(p -> p.toString().endsWith(EXT))
            .map(p -> p.getFileName().toString().replace(EXT, ""))
            .sorted()
            .collect(Collectors.toList());
      } catch (IOException e) {
         return Collections.emptyList();
      }
   }

   public static boolean exists(String name) {
      return Files.exists(ROOT_DIR.resolve(safe(name) + EXT));
   }
}
