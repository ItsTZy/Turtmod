package com.turtmod.cosmetics;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;

/**
 * Downloads cape texture PNGs (by URL) off-thread and registers them as GUI textures once ready, so the
 * cape grid can blit them. Mirrors {@link com.turtmod.cosmetics.skin.SkinLoader}: register only after the
 * image is fully decoded, on the render thread, so we never hand the renderer a half-loaded texture.
 */
public final class CapeTextureCache {

   /** A single cape's texture + its native dimensions (capes are 64x32, HD ones can be 64x64). */
   public static final class Entry {
      public volatile class_2960 id;
      public volatile int texW = 64;
      public volatile int texH = 32;
      volatile boolean loading;
      volatile boolean done;
   }

   private static final Map<String, Entry> CACHE = new ConcurrentHashMap<>();

   private CapeTextureCache() {
   }

   /** The texture entry for a cape URL; kicks off a download on first request. Never null. */
   public static Entry get(String url) {
      Entry e = CACHE.computeIfAbsent(url, u -> new Entry());
      if (!e.done && !e.loading && url != null && !url.isEmpty()) {
         e.loading = true;
         new Thread(() -> download(url, e), "turtmod-cape-tex").start();
      }
      return e;
   }

   private static void download(String url, Entry e) {
      byte[] bytes = null;
      try {
         HttpResponse<byte[]> res = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder(URI.create(url)).header("User-Agent", "TurtMod").GET().build(),
            HttpResponse.BodyHandlers.ofByteArray());
         if (res.statusCode() / 100 == 2) {
            bytes = res.body();
         }
      } catch (Exception ignored) {
      }
      final byte[] data = bytes;
      class_310 client = class_310.method_1551();
      if (client == null || data == null) {
         e.done = true;
         e.loading = false;
         return;
      }
      client.execute(() -> {
         try (java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(data)) {
            class_1011 img = class_1011.method_4309(in);
            if (img != null) {
               e.texW = img.method_4307();
               e.texH = img.method_4323();
               class_2960 id = class_2960.method_60655("turtmod", "cape/" + Math.abs(url.hashCode()));
               client.method_1531().method_4616(id, new class_1043(() -> "turtmod:cape", img));
               e.id = id;
            }
         } catch (Exception ignored) {
         } finally {
            e.done = true;
            e.loading = false;
         }
      });
   }
}
