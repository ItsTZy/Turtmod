package com.turtmod.utils;

import net.minecraft.class_3532;

public class Scroller {
   private float scrollAmount;
   private float targetScroll;
   private float minScroll;
   private float maxScroll;
   private float scrollSpeed;

   public Scroller() {
      this(0.0F, 0.0F, 0.0F);
   }

   public Scroller(float minScroll, float maxScroll, float initialScroll) {
      this.scrollSpeed = 0.15F;
      this.minScroll = minScroll;
      this.maxScroll = maxScroll;
      this.scrollAmount = class_3532.method_15363(initialScroll, minScroll, maxScroll);
      this.targetScroll = this.scrollAmount;
   }

   public void update(float delta) {
      float t = 1.0F - (float)Math.exp((double)(-this.scrollSpeed * delta));
      this.scrollAmount = class_3532.method_16439(t, this.scrollAmount, this.targetScroll);
      this.scrollAmount = class_3532.method_15363(this.scrollAmount, this.minScroll, this.maxScroll);
   }

   public void addScroll(double amount) {
      this.targetScroll = (float)((double)this.targetScroll + amount * (double)20.0F);
      this.targetScroll = class_3532.method_15363(this.targetScroll, this.minScroll, this.maxScroll);
   }

   public void setBounds(float minScroll, float maxScroll) {
      this.minScroll = minScroll;
      this.maxScroll = maxScroll;
      this.scrollAmount = class_3532.method_15363(this.scrollAmount, minScroll, maxScroll);
      this.targetScroll = class_3532.method_15363(this.targetScroll, minScroll, maxScroll);
   }

   public void setScrollSpeed(float speed) {
      this.scrollSpeed = class_3532.method_15363(speed, 0.01F, 1.0F);
   }

   public void jumpTo(float scroll) {
      this.scrollAmount = class_3532.method_15363(scroll, this.minScroll, this.maxScroll);
      this.targetScroll = this.scrollAmount;
   }

   public float getScrollAmount() {
      return this.scrollAmount;
   }

   public float getTargetScroll() {
      return this.targetScroll;
   }

   public boolean isScrolling() {
      return Math.abs(this.scrollAmount - this.targetScroll) > 0.5F;
   }

   public void reset() {
      this.jumpTo(this.minScroll);
   }
}
