package com.turtmod.chat;

import com.turtmod.TurtModClient;
import java.io.File;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_124;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import org.jetbrains.annotations.Nullable;

public final class BetterScreenshotFeature {
   private static final Pattern PNG_PATH_PATTERN = Pattern.compile("([A-Za-z]:\\\\[^\\s]+\\.png|[^\\s]+\\.png)");
   private static volatile String lastAbsoluteScreenshotPath = "";
   private static String lastPath = "";
   private static long lastPathAtMs = 0L;

   private BetterScreenshotFeature() {
   }

   public static @Nullable class_2561 buildActionMessage(class_2561 message) {
      if (TurtModClient.getConfig() != null && TurtModClient.getConfig().misc.enabled && TurtModClient.getConfig().hud.betterScreenshotActions) {
         String screenshotPath = extractScreenshotPath(message);
         if (screenshotPath == null) {
            return null;
         } else {
            screenshotPath = toAbsoluteScreenshotPath(screenshotPath);
            if (screenshotPath == null) {
               return null;
            } else {
               lastAbsoluteScreenshotPath = screenshotPath;
               long now = System.currentTimeMillis();
               if (screenshotPath.equals(lastPath) && now - lastPathAtMs < 800L) {
                  return null;
               } else {
                  lastPath = screenshotPath;
                  lastPathAtMs = now;
                  String folderPath = (new File(screenshotPath)).getParent();
                  class_5250 line = class_2561.method_43470("Screenshot ").method_27692(class_124.field_1063);
                  line.method_10852(buttonRunCommand("[View]", "/turtmod screenshot view", class_124.field_1076));
                  line.method_10852(class_2561.method_43470(" "));
                  line.method_10852(buttonRunCommand("[Open]", "/turtmod screenshot open", class_124.field_1060));
                  if (folderPath != null && !folderPath.isEmpty()) {
                     line.method_10852(class_2561.method_43470(" "));
                     line.method_10852(buttonRunCommand("[Folder]", "/turtmod screenshot folder", class_124.field_1075));
                  }

                  line.method_10852(class_2561.method_43470(" "));
                  line.method_10852(buttonRunCommand("[Copy]", "/turtmod screenshot copy", class_124.field_1065));
                  return line;
               }
            }
         }
      } else {
         return null;
      }
   }

   public static @Nullable String getLastAbsoluteScreenshotPath() {
      return lastAbsoluteScreenshotPath != null && !lastAbsoluteScreenshotPath.isEmpty() ? lastAbsoluteScreenshotPath : null;
   }

   private static @Nullable String extractScreenshotPath(class_2561 message) {
      String byClick = findOpenFileClick(message);
      if (byClick != null && byClick.toLowerCase(Locale.ROOT).endsWith(".png")) {
         return byClick;
      } else {
         String raw = message.getString();
         if (raw == null) {
            return null;
         } else {
            String lower = raw.toLowerCase(Locale.ROOT);
            if (lower.contains("screenshot") && lower.contains(".png")) {
               Matcher matcher = PNG_PATH_PATTERN.matcher(raw);
               return matcher.find() ? matcher.group(1) : null;
            } else {
               return null;
            }
         }
      }
   }

   private static @Nullable String findOpenFileClick(class_2561 text) {
      class_2558 event = text.method_10866().method_10970();
      if (event instanceof class_2558.class_10607 openFile) {
         return openFile.comp_3504();
      } else {
         for(class_2561 sibling : text.method_10855()) {
            String value = findOpenFileClick(sibling);
            if (value != null) {
               return value;
            }
         }

         return null;
      }
   }

   private static class_5250 buttonRunCommand(String label, String command, class_124 color) {
      class_2583 style = class_2583.field_24360.method_10977(color).method_10982(true).method_30938(true).method_10958(new class_2558.class_10609(command));
      return class_2561.method_43470(label).method_10862(style);
   }

   private static @Nullable String toAbsoluteScreenshotPath(String rawPath) {
      try {
         File file = new File(rawPath);
         if (file.isAbsolute()) {
            return file.getCanonicalPath();
         } else {
            class_310 mc = class_310.method_1551();
            File base = mc != null ? new File(mc.field_1697, "screenshots") : new File("screenshots");
            return (new File(base, rawPath)).getCanonicalPath();
         }
      } catch (Exception var4) {
         return null;
      }
   }
}
