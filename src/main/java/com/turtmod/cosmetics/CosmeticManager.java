package com.turtmod.cosmetics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CosmeticManager {
   private static final Logger LOGGER = LoggerFactory.getLogger("TurtMod Cosmetics");
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static final Path COSMETICS_DIR = FabricLoader.getInstance().getConfigDir().resolve("turtmod-cosmetics");
   private static final Path SKINS_DIR;
   private static final Path PROFILES_FILE;
   private static List<CosmeticProfile> profiles;
   private static CosmeticProfile activeProfile;
   private static class_2960 customSkinTexture;
   private static class_2960 previewSkinTexture;
   private static boolean hasCustomSkin;
   private static boolean uiPreviewMode;

   public static void init() {
      try {
         Files.createDirectories(COSMETICS_DIR);
         Files.createDirectories(SKINS_DIR);
         loadProfiles();
         LOGGER.info("Cosmetics manager initialized");
      } catch (IOException e) {
         LOGGER.error("Failed to initialize cosmetics manager", e);
      }

   }

   public static void loadProfiles() {
      if (Files.exists(PROFILES_FILE, new LinkOption[0])) {
         try {
            String json = Files.readString(PROFILES_FILE);
            profiles = (List)GSON.fromJson(json, (new TypeToken<List<CosmeticProfile>>() {
            }).getType());
            if (profiles != null && !profiles.isEmpty()) {
               activeProfile = (CosmeticProfile)profiles.get(0);
               hasCustomSkin = activeProfile.enableSkin && activeProfile.skinPath != null && !activeProfile.skinPath.isEmpty();
            }
         } catch (IOException var1) {
            profiles = new ArrayList();
         }
      }

      if (profiles == null) {
         profiles = new ArrayList();
      }

   }

   public static void saveProfiles() {
      try {
         Files.createDirectories(COSMETICS_DIR);
         Files.writeString(PROFILES_FILE, GSON.toJson(profiles));
      } catch (IOException e) {
         LOGGER.error("Failed to save cosmetic profiles", e);
      }

   }

   public static List<CosmeticProfile> getProfiles() {
      return profiles;
   }

   public static CosmeticProfile getActiveProfile() {
      return activeProfile;
   }

   public static void setActiveProfile(CosmeticProfile profile) {
      activeProfile = profile;
      if (profile != null) {
         hasCustomSkin = profile.enableSkin && profile.skinPath != null && !profile.skinPath.isEmpty();
         applyProfile(profile);
      } else {
         hasCustomSkin = false;
         customSkinTexture = null;
      }

   }

   public static void addProfile(CosmeticProfile profile) {
      profiles.add(profile);
      saveProfiles();
   }

   public static void removeProfile(CosmeticProfile profile) {
      profiles.remove(profile);
      if (activeProfile == profile) {
         activeProfile = profiles.isEmpty() ? null : (CosmeticProfile)profiles.get(0);
         if (activeProfile != null) {
            applyProfile(activeProfile);
         }
      }

      saveProfiles();
   }

   public static void applyProfile(CosmeticProfile profile) {
      if (profile != null) {
         if (profile.enableSkin && profile.skinPath != null && !profile.skinPath.isEmpty()) {
            customSkinTexture = loadTextureFromFile(profile.skinPath, "active_skin");
         } else {
            customSkinTexture = null;
         }

      }
   }

   public static class_2960 loadPreviewSkin(String path, String name) {
      previewSkinTexture = loadTextureFromFile(path, "preview_" + name);
      return previewSkinTexture;
   }

   private static class_2960 loadTextureFromFile(String skinPath, String debugName) {
      class_310 client = class_310.method_1551();
      if (client == null) {
         return null;
      } else {
         File skinFile = new File(skinPath);
         if (!skinFile.exists()) {
            return null;
         } else {
            String fileName = skinFile.getName().toLowerCase().replaceAll("[^a-z0-9/._-]", "_");
            long modified = skinFile.lastModified();
            class_2960 id = class_2960.method_60655("turtmod", "cosmetics/" + modified + "_" + fileName);

            try {
               InputStream in = Files.newInputStream(skinFile.toPath());

               class_1043 texture;
               label74: {
                  class_2960 var11;
                  label75: {
                     try {
                        class_1011 image = class_1011.method_4309(in);
                        if (image == null) {
                           texture = null;
                           break label74;
                        }

                        image = normalizeSkinImage(image);
                        if (image.method_4307() == 64 && image.method_4323() == 64) {
                           texture = new class_1043(() -> "turtmod:" + debugName, image);
                           client.method_1531().method_4616(id, texture);
                           Logger var10000 = LOGGER;
                           String var10001 = String.valueOf(id);
                           var10000.info("Registered custom texture: " + var10001 + " for " + debugName);
                           var11 = id;
                           break label75;
                        }

                        LOGGER.warn("Unsupported skin size {}x{} for {}", new Object[]{image.method_4307(), image.method_4323(), debugName});
                        image.close();
                        texture = null;
                     } catch (Throwable var13) {
                        if (in != null) {
                           try {
                              in.close();
                           } catch (Throwable var12) {
                              var13.addSuppressed(var12);
                           }
                        }

                        throw var13;
                     }

                     if (in != null) {
                        in.close();
                     }

                     return null;
                  }

                  if (in != null) {
                     in.close();
                  }

                  return var11;
               }

               if (in != null) {
                  in.close();
               }

               return null;
            } catch (Exception e) {
               LOGGER.error("Failed to load skin texture: " + debugName, e);
               return null;
            }
         }
      }
   }

   public static class_1011 normalizeSkinImage(class_1011 source) {
      if (source.method_4307() == 64 && source.method_4323() == 64) {
         return source;
      } else if (source.method_4307() == 64 && source.method_4323() == 32) {
         class_1011 expanded = new class_1011(64, 64, true);

         for(int y = 0; y < 32; ++y) {
            for(int x = 0; x < 64; ++x) {
               expanded.method_61941(x, y, source.method_61940(x, y));
            }
         }

         source.close();
         return expanded;
      } else {
         return source;
      }
   }

   public static class_2960 getCustomSkinTexture() {
      return customSkinTexture;
   }

   public static class_2960 getPreviewSkinTexture() {
      return previewSkinTexture;
   }

   public static void setPreviewSkinTexture(class_2960 id) {
      previewSkinTexture = id;
   }

   public static boolean isUiPreviewMode() {
      return uiPreviewMode;
   }

   public static void setUiPreviewMode(boolean mode) {
      uiPreviewMode = mode;
   }

   public static boolean hasCustomSkin() {
      return hasCustomSkin && customSkinTexture != null;
   }

   public static Path getSkinsDirectory() {
      return SKINS_DIR;
   }

   public static UploadResult uploadSkinToMinecraftProfile(String skinPath, boolean slimVariant) {
      class_310 client = class_310.method_1551();
      if (client != null && client.method_1548() != null) {
         String accessToken = client.method_1548().method_1674();
         if (accessToken != null && !accessToken.isBlank()) {
            File skinFile = new File(skinPath);
            if (skinFile.exists() && skinFile.isFile()) {
               String fileName = skinFile.getName().toLowerCase(Locale.ROOT);
               if (!fileName.endsWith(".png")) {
                  return CosmeticManager.UploadResult.fail("Skin must be a .png file.");
               } else {
                  String variant = slimVariant ? "slim" : "classic";
                  String boundary = "----TurtModBoundary" + System.currentTimeMillis();

                  try {
                     byte[] fileBytes = Files.readAllBytes(skinFile.toPath());
                     byte[] body = buildSkinMultipartBody(boundary, variant, skinFile.getName(), fileBytes);
                     HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.minecraftservices.com/minecraft/profile/skins")).header("Authorization", "Bearer " + accessToken).header("Content-Type", "multipart/form-data; boundary=" + boundary).POST(BodyPublishers.ofByteArray(body)).build();
                     HttpResponse<String> response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString());
                     int code = response.statusCode();
                     if (code >= 200 && code < 300) {
                        return CosmeticManager.UploadResult.ok("Global skin updated. Rejoin servers if cache is stale.");
                     } else {
                        String bodySnippet = response.body() == null ? "" : (String)response.body();
                        if (bodySnippet.length() > 140) {
                           bodySnippet = bodySnippet.substring(0, 140) + "...";
                        }

                        return CosmeticManager.UploadResult.fail("Upload failed (" + code + "): " + bodySnippet);
                     }
                  } catch (Exception e) {
                     LOGGER.error("Failed to upload skin to profile", e);
                     return CosmeticManager.UploadResult.fail("Upload failed: " + e.getClass().getSimpleName());
                  }
               }
            } else {
               return CosmeticManager.UploadResult.fail("Skin file not found.");
            }
         } else {
            return CosmeticManager.UploadResult.fail("No access token found. Re-login to Minecraft.");
         }
      } else {
         return CosmeticManager.UploadResult.fail("Minecraft session not available.");
      }
   }

   private static byte[] buildSkinMultipartBody(String boundary, String variant, String fileName, byte[] fileBytes) throws IOException {
      byte[] line = "\r\n".getBytes(StandardCharsets.UTF_8);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
      out.write("Content-Disposition: form-data; name=\"variant\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
      out.write(variant.getBytes(StandardCharsets.UTF_8));
      out.write(line);
      out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
      out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n").getBytes(StandardCharsets.UTF_8));
      out.write("Content-Type: image/png\r\n\r\n".getBytes(StandardCharsets.UTF_8));
      out.write(fileBytes);
      out.write(line);
      out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
      return out.toByteArray();
   }

   static {
      SKINS_DIR = COSMETICS_DIR.resolve("skins");
      PROFILES_FILE = COSMETICS_DIR.resolve("profiles.json");
      profiles = new ArrayList();
      activeProfile = null;
      customSkinTexture = null;
      previewSkinTexture = null;
      hasCustomSkin = false;
      uiPreviewMode = false;
   }

   public static record UploadResult(boolean success, String message) {
      public static UploadResult ok(String message) {
         return new UploadResult(true, message);
      }

      public static UploadResult fail(String message) {
         return new UploadResult(false, message);
      }
   }
}
