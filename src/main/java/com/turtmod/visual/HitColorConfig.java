package com.turtmod.visual;

import com.turtmod.event.OverlayReloadListener;

public class HitColorConfig {
   public boolean enabled = true;
   public int color = 1308557312;
   public int alpha = 73;

   public int getTintColor() {
      return this.alpha << 24 | this.color & 16777215;
   }

   public void setColor(int argb) {
      this.color = argb;
      OverlayReloadListener.callEvent();
   }

   public void setAlpha(int a) {
      this.alpha = a;
      OverlayReloadListener.callEvent();
   }
}
