package com.turtmod.cosmetics.skin;

import com.turtmod.utils.TurtLogger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;

/**
 * Async-safe, ref-counted skin texture loader.
 * Follows the SkinShuffle pattern: texture is registered AFTER it is fully loaded,
 * so the render thread never gets a null/missing resource.
 */
public final class SkinLoader {

   public static final class SkinHandle implements AutoCloseable {
      private volatile class_2960 textureId;
      private volatile boolean fetching = false;
      private volatile boolean fetched = false;
      private final String uniqueKey;
      private final Path path;

      private SkinHandle(Path path, String uniqueKey) {
         this.path = path;
         this.uniqueKey = uniqueKey;
      }

      /** Returns the texture ID once loaded, or null while loading. */
      public class_2960 getTexture() {
         if (!fetched && !fetching) {
            fetch();
         }
         return textureId;
      }

      public boolean isLoaded() { return fetched && !fetching; }

      private void fetch() {
         fetching = true;
         class_2960 id = class_2960.method_60655("turtmod", "skin/file/" + Math.abs(uniqueKey.hashCode()));
         class_310 client = class_310.method_1551();
         if (client == null) { fetching = false; return; }

         // Load + register on render thread
         client.execute(() -> {
            try {
               class_1011 image = loadAndNormalize(path);
               if (image == null) { fetching = false; fetched = true; return; }
               class_1043 texture = new class_1043(() -> "turtmod:skin/" + uniqueKey, image);
               client.method_1531().method_4616(id, texture);
               textureId = id;
               TurtLogger.info("[turtmod] Skin registered: " + id);
            } catch (Exception e) {
               TurtLogger.warning("[turtmod] Skin load failed: " + path + " — " + e.getMessage());
            } finally {
               fetched = true;
               fetching = false;
            }
         });
      }

      @Override
      public void close() {
         // Texture stays registered in TextureManager until resource reload; just drop our reference
         textureId = null;
         fetched = false;
         fetching = false;
      }

      private static class_1011 loadAndNormalize(Path path) throws IOException {
         try (InputStream in = Files.newInputStream(path)) {
            class_1011 img = class_1011.method_4309(in);
            if (img == null) return null;
            // 64x32 legacy → expand to 64x64
            if (img.method_4307() == 64 && img.method_4323() == 32) {
               class_1011 expanded = new class_1011(64, 64, true);
               for (int y = 0; y < 32; y++)
                  for (int x = 0; x < 64; x++)
                     expanded.method_61941(x, y, img.method_61940(x, y));
               img.close();
               return expanded;
            }
            return img;
         }
      }
   }

   // ── Cache ──────────────────────────────────────────────────────────────────

   private static final Map<String, SkinHandle> CACHE = new HashMap<>();

   /**
    * Returns a handle for the given skin file, creating and caching one if absent.
    * The key is path + lastModified so file edits automatically create a new handle.
    */
   public static SkinHandle get(Path path) {
      if (path == null || !Files.isReadable(path)) return null;
      try {
         long modified = Files.getLastModifiedTime(path).toMillis();
         String key = path.toAbsolutePath().toString() + "_" + modified;
         SkinHandle existing = CACHE.get(key);
         if (existing != null) return existing;
         // Invalidate any stale entry for the same path
         CACHE.entrySet().removeIf(e -> {
            if (e.getKey().startsWith(path.toAbsolutePath().toString() + "_")) {
               e.getValue().close();
               return true;
            }
            return false;
         });
         SkinHandle handle = new SkinHandle(path, key);
         CACHE.put(key, handle);
         return handle;
      } catch (IOException e) {
         TurtLogger.warning("[turtmod] SkinLoader.get failed for " + path + ": " + e.getMessage());
         return null;
      }
   }

   /** Get texture ID directly (null while loading — call again next frame). */
   public static class_2960 getTexture(Path path) {
      SkinHandle h = get(path);
      return h == null ? null : h.getTexture();
   }

   public static void clearAll() {
      CACHE.values().forEach(SkinHandle::close);
      CACHE.clear();
   }

   private SkinLoader() {}
}
