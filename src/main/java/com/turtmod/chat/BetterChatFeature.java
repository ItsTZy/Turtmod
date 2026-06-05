package com.turtmod.chat;

public class BetterChatFeature {
   private static final String[][] EMOJI_REPLACEMENTS = new String[0][];

   // Captured chat history (newest first) for the searchable Chat History screen.
   private static final java.util.ArrayDeque<String> HISTORY = new java.util.ArrayDeque();
   private static final int MAX_HISTORY = 300;

   public static void recordHistory(String message) {
      if (message == null || message.isBlank()) {
         return;
      }
      synchronized (HISTORY) {
         HISTORY.addFirst(message);
         while (HISTORY.size() > MAX_HISTORY) {
            HISTORY.removeLast();
         }
      }
   }

   public static java.util.List<String> getHistory() {
      synchronized (HISTORY) {
         return new java.util.ArrayList(HISTORY);
      }
   }

   public static String[][] getEmojiReplacements() {
      return EMOJI_REPLACEMENTS;
   }

   public static String replaceEmojis(String message) {
      return message;
   }

   public static int getEmojiCount() {
      return EMOJI_REPLACEMENTS.length;
   }
}
