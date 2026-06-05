package com.turtmod.utils;

import java.util.ArrayList;
import java.util.List;

public final class CPSTracker {
   public static final List<Long> leftClicks = new ArrayList();
   public static final List<Long> rightClicks = new ArrayList();

   private CPSTracker() {
   }

   public static void onLeftClick() {
      leftClicks.add(System.currentTimeMillis());
   }

   public static void onRightClick() {
      rightClicks.add(System.currentTimeMillis());
   }

   public static void tick() {
      long now = System.currentTimeMillis();
      leftClicks.removeIf((time) -> now - time > 1000L);
      rightClicks.removeIf((time) -> now - time > 1000L);
   }

   public static int getLeftCPS() {
      return leftClicks.size();
   }

   public static int getRightCPS() {
      return rightClicks.size();
   }

   public static int getCombinedCPS() {
      return leftClicks.size() + rightClicks.size();
   }
}
