package com.turtmod.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TurtLogger {
   private static final Logger LOGGER = LoggerFactory.getLogger("TurtMod");
   private static final List<LogEntry> logs = new ArrayList();
   private static final int MAX_LOGS = 100;

   public static void info(String message) {
      log(TurtLogger.LogLevel.INFO, message);
      LOGGER.info(message);
   }

   public static void warning(String message) {
      log(TurtLogger.LogLevel.WARNING, message);
      LOGGER.warn(message);
   }

   public static void error(String message) {
      log(TurtLogger.LogLevel.ERROR, message);
      LOGGER.error(message);
   }

   public static void debug(String message) {
      log(TurtLogger.LogLevel.DEBUG, message);
      LOGGER.debug(message);
   }

   public static void success(String message) {
      log(TurtLogger.LogLevel.SUCCESS, message);
      LOGGER.info(message);
   }

   private static void log(LogLevel level, String message) {
      logs.add(new LogEntry(level, message, System.currentTimeMillis()));

      while(logs.size() > 100) {
         logs.remove(0);
      }

   }

   public static List<LogEntry> getRecentLogs() {
      return Collections.unmodifiableList(logs);
   }

   public static void clearLogs() {
      logs.clear();
   }

   public static enum LogLevel {
      INFO("§7"),
      WARNING("§e"),
      ERROR("§c"),
      DEBUG("§8"),
      SUCCESS("§a");

      private final String colorCode;

      private LogLevel(String colorCode) {
         this.colorCode = colorCode;
      }

      public String getColorCode() {
         return this.colorCode;
      }

      // $FF: synthetic method
      private static LogLevel[] $values() {
         return new LogLevel[]{INFO, WARNING, ERROR, DEBUG, SUCCESS};
      }
   }

   public static record LogEntry(LogLevel level, String message, long timestamp) {
      public String getFormatted() {
         String var10000 = this.level.colorCode;
         return var10000 + "[" + this.level.name() + "] " + this.message;
      }
   }
}
