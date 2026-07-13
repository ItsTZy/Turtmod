package com.turtmod.chat;

import com.turtmod.gallery.ScreenshotViewScreen;
import com.turtmod.utils.ImageClipboardUtils;
import java.io.File;
import net.minecraft.class_124;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_310;

/** Local screenshot actions used by the corner preview + gallery: view, open, copy-to-clipboard. */
public final class ScreenshotUploadFeature {
   private ScreenshotUploadFeature() {
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
