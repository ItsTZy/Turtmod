package com.turtmod.chat;

import com.turtmod.gallery.ScreenshotViewScreen;
import com.turtmod.utils.ImageClipboardUtils;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.minecraft.class_124;
import net.minecraft.class_156;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;

public final class ScreenshotUploadFeature {
   private ScreenshotUploadFeature() {
   }

   public static void uploadLastScreenshot(class_310 client) {
      String path = BetterScreenshotFeature.getLastAbsoluteScreenshotPath();
      if (path == null) {
         send(client, class_2561.method_43470("No recent screenshot found. Take one first.").method_27692(class_124.field_1061));
      } else {
         File file = new File(path);
         if (file.exists() && file.isFile()) {
            send(client, class_2561.method_43470("Uploading screenshot...").method_27692(class_124.field_1080));
            Thread thread = new Thread(() -> doUpload(client, file.toPath()), "turtmod-screenshot-upload");
            thread.setDaemon(true);
            thread.start();
         } else {
            send(client, class_2561.method_43470("Screenshot file not found: " + path).method_27692(class_124.field_1061));
         }
      }
   }

   public static void openLastScreenshot(class_310 client) {
      String path = BetterScreenshotFeature.getLastAbsoluteScreenshotPath();
      if (path == null) {
         send(client, class_2561.method_43470("No recent screenshot found.").method_27692(class_124.field_1061));
      } else {
         File file = new File(path);
         if (file.exists() && file.isFile()) {
            openFile(client, file);
         } else {
            send(client, class_2561.method_43470("Screenshot file not found.").method_27692(class_124.field_1061));
         }
      }
   }

   public static void viewLastScreenshot(class_310 client) {
      String path = BetterScreenshotFeature.getLastAbsoluteScreenshotPath();
      if (path == null) {
         send(client, class_2561.method_43470("No recent screenshot found.").method_27692(class_124.field_1061));
      } else {
         File file = new File(path);
         if (file.exists() && file.isFile()) {
            client.execute(() -> client.method_1507(new ScreenshotViewScreen(client.field_1755, file)));
         } else {
            send(client, class_2561.method_43470("Screenshot file not found.").method_27692(class_124.field_1061));
         }
      }
   }

   public static void openScreenshotFolder(class_310 client) {
      String path = BetterScreenshotFeature.getLastAbsoluteScreenshotPath();
      File folder;
      if (path == null) {
         folder = new File(client.field_1697, "screenshots");
      } else {
         File file = new File(path);
         folder = file.getParentFile() == null ? new File(client.field_1697, "screenshots") : file.getParentFile();
      }

      if (!folder.exists()) {
         send(client, class_2561.method_43470("Screenshot folder not found.").method_27692(class_124.field_1061));
      } else {
         openFile(client, folder);
      }
   }

   public static void copyLastScreenshotPath(class_310 client) {
      String path = BetterScreenshotFeature.getLastAbsoluteScreenshotPath();
      if (path == null) {
         send(client, class_2561.method_43470("No recent screenshot to copy.").method_27692(class_124.field_1061));
      } else {
         File file = new File(path);
         if (file.exists() && file.isFile()) {
            client.execute(() -> {
               if (ImageClipboardUtils.copyImageToClipboard(file)) {
                  send(client, class_2561.method_43470("Copied screenshot image to clipboard!").method_27692(class_124.field_1060));
               } else {
                  send(client, class_2561.method_43470("Could not copy screenshot image to clipboard.").method_27692(class_124.field_1061));
               }

            });
         } else {
            send(client, class_2561.method_43470("Screenshot file not found.").method_27692(class_124.field_1061));
         }
      }
   }

   private static void doUpload(class_310 client, Path filePath) {
      try {
         String boundary = "----TurtModBoundary" + String.valueOf(UUID.randomUUID());
         byte[] body = multipart(boundary, filePath);
         HttpRequest request = HttpRequest.newBuilder(URI.create("https://0x0.st")).header("Content-Type", "multipart/form-data; boundary=" + boundary).POST(BodyPublishers.ofByteArray(body)).build();
         HttpClient http = HttpClient.newBuilder().followRedirects(Redirect.NORMAL).build();
         HttpResponse<String> response = http.send(request, BodyHandlers.ofString());
         String link = response.body() == null ? "" : ((String)response.body()).trim();
         if (response.statusCode() / 100 != 2 || !link.startsWith("http")) {
            send(client, class_2561.method_43470("Upload failed (" + response.statusCode() + ").").method_27692(class_124.field_1061));
            return;
         }

         client.execute(() -> {
            client.field_1774.method_1455(link);
            class_2561 clickable = class_2561.method_43470("[Open Link]").method_10862(class_2583.field_24360.method_10977(class_124.field_1075).method_30938(true).method_10958(new class_2558.class_10608(URI.create(link))));
            send(client, class_2561.method_43470("Uploaded: ").method_27692(class_124.field_1060).method_10852(clickable).method_10852(class_2561.method_43470(" (copied)")));
         });
      } catch (Exception e) {
         send(client, class_2561.method_43470("Upload error: " + e.getMessage()).method_27692(class_124.field_1061));
      }

   }

   private static void openFile(class_310 client, File target) {
      try {
         class_156.method_668().method_672(target);
         send(client, class_2561.method_43470("Opened: " + target.getName()).method_27692(class_124.field_1060));
      } catch (Exception var3) {
         try {
            if (java.awt.Desktop.isDesktopSupported()) {
               java.awt.Desktop.getDesktop().open(target);
               send(client, class_2561.method_43470("Opened (fallback): " + target.getName()).method_27692(class_124.field_1060));
            } else {
               throw new UnsupportedOperationException("Desktop not supported");
            }
         } catch (Exception e) {
            send(client, class_2561.method_43470("Could not open file/folder.").method_27692(class_124.field_1061));
         }
      }
   }

   private static byte[] multipart(String boundary, Path filePath) throws IOException {
      String fileName = filePath.getFileName().toString();
      byte[] fileBytes = Files.readAllBytes(filePath);
      String head = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\nContent-Type: image/png\r\n\r\n";
      String tail = "\r\n--" + boundary + "--\r\n";
      byte[] headBytes = head.getBytes();
      byte[] tailBytes = tail.getBytes();
      byte[] out = new byte[headBytes.length + fileBytes.length + tailBytes.length];
      System.arraycopy(headBytes, 0, out, 0, headBytes.length);
      System.arraycopy(fileBytes, 0, out, headBytes.length, fileBytes.length);
      System.arraycopy(tailBytes, 0, out, headBytes.length + fileBytes.length, tailBytes.length);
      return out;
   }

   private static void send(class_310 client, class_2561 text) {
      if (client != null) {
         client.execute(() -> {
            if (client.field_1705 != null && client.field_1705.method_1743() != null) {
               client.field_1705.method_1743().method_1812(text);
            }

         });
      }
   }
}
