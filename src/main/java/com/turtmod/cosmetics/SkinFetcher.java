package com.turtmod.cosmetics;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

/**
 * Looks a player up by name on Mojang's public API and downloads their current skin PNG.
 *
 * <p>username -> UUID -> profile (a base64 "textures" property) -> skin URL -> PNG bytes. All calls are
 * plain HTTP with no auth, so this is safe to run for any username. Always call off the render thread.
 */
public final class SkinFetcher {
   private static final String NAME_URL = "https://api.mojang.com/users/profiles/minecraft/";
   private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

   private SkinFetcher() {
   }

   /** The player's skin PNG bytes, or null if the name doesn't exist or anything goes wrong. */
   public static byte[] fetchSkin(String username) {
      try {
         HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

         String idJson = get(http, NAME_URL + username.replaceAll("[^A-Za-z0-9_]", ""));
         if (idJson == null) {
            return null;
         }
         String uuid = extract(idJson, "id");
         if (uuid == null) {
            return null;
         }

         String profileJson = get(http, PROFILE_URL + uuid);
         if (profileJson == null) {
            return null;
         }
         String b64 = extract(profileJson, "value");
         if (b64 == null) {
            return null;
         }
         String decoded = new String(Base64.getDecoder().decode(b64), java.nio.charset.StandardCharsets.UTF_8);

         // {"textures":{"SKIN":{"url":"https://textures.minecraft.net/texture/..."}}}
         int skinIdx = decoded.indexOf("\"SKIN\"");
         if (skinIdx < 0) {
            return null;
         }
         String url = extract(decoded.substring(skinIdx), "url");
         if (url == null) {
            return null;
         }

         HttpResponse<byte[]> png = http.send(
            HttpRequest.newBuilder(URI.create(url)).header("User-Agent", "TurtMod-SkinEditor").GET().build(),
            HttpResponse.BodyHandlers.ofByteArray());
         return png.statusCode() / 100 == 2 ? png.body() : null;
      } catch (Exception e) {
         return null;
      }
   }

   private static String get(HttpClient http, String url) throws Exception {
      HttpResponse<String> r = http.send(
         HttpRequest.newBuilder(URI.create(url)).header("User-Agent", "TurtMod-SkinEditor").GET().build(),
         HttpResponse.BodyHandlers.ofString());
      return r.statusCode() / 100 == 2 ? r.body() : null;
   }

   /** Minimal JSON string-field read - enough for these small, fixed-shape payloads. */
   private static String extract(String json, String field) {
      String key = "\"" + field + "\"";
      int i = json.indexOf(key);
      if (i < 0) {
         return null;
      }
      int colon = json.indexOf(':', i + key.length());
      if (colon < 0) {
         return null;
      }
      int q1 = json.indexOf('"', colon + 1);
      if (q1 < 0) {
         return null;
      }
      int q2 = json.indexOf('"', q1 + 1);
      return q2 < 0 ? null : json.substring(q1 + 1, q2);
   }
}
