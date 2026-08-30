package com.turtmod.utils;

import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;

public final class ImageClipboardUtils {
   private ImageClipboardUtils() {
   }

   public static boolean copyImageToClipboard(File imageFile) {
      try {
         if (GraphicsEnvironment.isHeadless()) {
            System.err.println("ImageClipboardUtils: Headless environment detected, attempting OS fallback.");
            return osFallback(imageFile);
         } else {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
               System.err.println("ImageClipboardUtils: Failed to read image: " + imageFile.getAbsolutePath());
               return false;
            } else {
               BufferedImage argbImage = new BufferedImage(image.getWidth(), image.getHeight(), 2);
               Graphics2D g = argbImage.createGraphics();
               g.drawImage(image, 0, 0, (ImageObserver)null);
               g.dispose();
               AtomicBoolean success = new AtomicBoolean(false);
               Runnable setClipboard = () -> {
                  try {
                     Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                     Transferable transferable = new Transferable() {
                        public DataFlavor[] getTransferDataFlavors() {
                           return new DataFlavor[]{DataFlavor.imageFlavor};
                        }

                        public boolean isDataFlavorSupported(DataFlavor flavor) {
                           return DataFlavor.imageFlavor.equals(flavor);
                        }

                        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                           if (!DataFlavor.imageFlavor.equals(flavor)) {
                              throw new UnsupportedFlavorException(flavor);
                           } else {
                              return argbImage;
                           }
                        }
                     };
                     clipboard.setContents(transferable, (ClipboardOwner)null);
                     success.set(true);
                  } catch (Exception e) {
                     System.err.println("ImageClipboardUtils: AWT Clipboard failed: " + e.getMessage());
                  }

               };
               if (EventQueue.isDispatchThread()) {
                  setClipboard.run();
               } else {
                  try {
                     EventQueue.invokeAndWait(setClipboard);
                  } catch (InterruptedException var8) {
                     Thread.currentThread().interrupt();
                     EventQueue.invokeLater(setClipboard);
                  }
               }

               return !success.get() ? osFallback(imageFile) : success.get();
            }
         }
      } catch (Exception e) {
         System.err.println("ImageClipboardUtils: Critical exception: " + e.getMessage());
         e.printStackTrace();
         return false;
      }
   }

   private static boolean osFallback(File imageFile) {
      String os = System.getProperty("os.name").toLowerCase();

      try {
         if (os.contains("win")) {
            String script = String.format("powershell -command \"Add-Type -AssemblyName System.Windows.Forms; $img = [System.Drawing.Image]::FromFile('%s'); [System.Windows.Forms.Clipboard]::SetImage($img)\"", imageFile.getAbsolutePath().replace("'", "''"));
            Runtime.getRuntime().exec(script);
            return true;
         }

         if (os.contains("mac")) {
            String script = String.format("osascript -e 'set the clipboard to (read (POSIX file \"%s\") as JPEG picture)'", imageFile.getAbsolutePath());
            Runtime.getRuntime().exec(script);
            return true;
         }
      } catch (IOException e) {
         System.err.println("ImageClipboardUtils: OS fallback failed: " + e.getMessage());
      }

      return false;
   }
}
