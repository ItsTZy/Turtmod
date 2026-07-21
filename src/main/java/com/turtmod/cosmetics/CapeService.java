package com.turtmod.cosmetics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_310;

/**
 * Talks to Mojang's authenticated account API to list the capes the logged-in account owns and to switch
 * which one is active. Uses the running session's access token as a Bearer credential — the same one the
 * launcher uses, so it works for any legitimately logged-in Microsoft/Mojang account.
 *
 * <p>Switching a cape here is a REAL account change: it persists on the account and everyone sees it on
 * every server, exactly like changing it on minecraft.net. All calls block on the network, so always run
 * them off the render thread.
 */
public final class CapeService {

   private static final String PROFILE = "https://api.minecraftservices.com/minecraft/profile";
   private static final String ACTIVE_CAPE = "https://api.minecraftservices.com/minecraft/profile/capes/active";

   private CapeService() {
   }

   /** One cape the account owns. {@code active} marks the currently-worn one. */
   public static final class Cape {
      public final String id;
      public final String alias;   // human name, e.g. "Migrator"
      public final String url;     // texture PNG url
      public final boolean active;

      Cape(String id, String alias, String url, boolean active) {
         this.id = id;
         this.alias = alias;
         this.url = url;
         this.active = active;
      }
   }

   /** The access token of the current session, or null if unavailable. */
   private static String token() {
      try {
         return class_310.method_1551().method_1548().method_1674();
      } catch (Exception e) {
         return null;
      }
   }

   private static HttpClient http() {
      return HttpClient.newBuilder()
         .connectTimeout(Duration.ofSeconds(10))
         .followRedirects(HttpClient.Redirect.NORMAL)
         .build();
   }

   /**
    * Fetch every cape the account owns. Returns an empty list if the account owns none, or null if the
    * request failed (not logged in, offline, token rejected, …) so the caller can tell the two apart.
    */
   public static List<Cape> fetchOwnedCapes() {
      String tok = token();
      if (tok == null || tok.isEmpty()) {
         return null;
      }
      try {
         HttpResponse<String> res = http().send(
            HttpRequest.newBuilder(URI.create(PROFILE))
               .header("Authorization", "Bearer " + tok)
               .header("Accept", "application/json")
               .GET().build(),
            HttpResponse.BodyHandlers.ofString());
         if (res.statusCode() / 100 != 2) {
            return null;
         }
         JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
         List<Cape> out = new ArrayList<>();
         if (root.has("capes") && root.get("capes").isJsonArray()) {
            JsonArray capes = root.getAsJsonArray("capes");
            for (JsonElement el : capes) {
               JsonObject c = el.getAsJsonObject();
               String id = str(c, "id");
               if (id == null) {
                  continue;
               }
               String alias = str(c, "alias");
               String url = str(c, "url");
               boolean active = "ACTIVE".equalsIgnoreCase(str(c, "state"));
               out.add(new Cape(id, alias != null ? alias : "Cape", url, active));
            }
         }
         return out;
      } catch (Exception e) {
         return null;
      }
   }

   /** Make {@code capeId} the account's active cape. Returns true on success. */
   public static boolean setActive(String capeId) {
      String tok = token();
      if (tok == null || tok.isEmpty() || capeId == null) {
         return false;
      }
      try {
         String body = "{\"capeId\":\"" + capeId.replace("\"", "") + "\"}";
         HttpResponse<String> res = http().send(
            HttpRequest.newBuilder(URI.create(ACTIVE_CAPE))
               .header("Authorization", "Bearer " + tok)
               .header("Content-Type", "application/json")
               .PUT(HttpRequest.BodyPublishers.ofString(body)).build(),
            HttpResponse.BodyHandlers.ofString());
         return res.statusCode() / 100 == 2;
      } catch (Exception e) {
         return false;
      }
   }

   /** Remove the active cape (show no cape). Returns true on success. */
   public static boolean removeActive() {
      String tok = token();
      if (tok == null || tok.isEmpty()) {
         return false;
      }
      try {
         HttpResponse<String> res = http().send(
            HttpRequest.newBuilder(URI.create(ACTIVE_CAPE))
               .header("Authorization", "Bearer " + tok)
               .DELETE().build(),
            HttpResponse.BodyHandlers.ofString());
         return res.statusCode() / 100 == 2;
      } catch (Exception e) {
         return false;
      }
   }

   private static String str(JsonObject o, String key) {
      return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsString() : null;
   }
}
