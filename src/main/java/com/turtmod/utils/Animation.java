package com.turtmod.utils;

import net.minecraft.class_3532;

public class Animation {
   private float currentValue;
   private float targetValue;
   private float speed;
   private Runnable onAnimating;

   public Animation(float initialValue, float speed) {
      this(initialValue, speed, (Runnable)null);
   }

   public Animation(float initialValue, float speed, Runnable onAnimating) {
      this.currentValue = initialValue;
      this.targetValue = initialValue;
      this.speed = speed;
      this.onAnimating = onAnimating;
   }

   public void update(float delta) {
      float t = 1.0F - (float)Math.exp((double)(-this.speed * delta));
      float newValue = class_3532.method_16439(t, this.currentValue, this.targetValue);
      if (Math.abs(newValue - this.targetValue) > 0.001F && this.onAnimating != null) {
         this.onAnimating.run();
      }

      this.currentValue = newValue;
   }

   public void setTargetValue(float targetValue) {
      this.targetValue = targetValue;
   }

   public float getCurrentValue() {
      return this.currentValue;
   }

   public void jumpTo(float value) {
      this.currentValue = value;
      this.targetValue = value;
   }

   public boolean isAnimating() {
      return Math.abs(this.currentValue - this.targetValue) > 0.01F;
   }

   public float getTargetValue() {
      return this.targetValue;
   }

   public void setSpeed(float speed) {
      this.speed = speed;
   }

   public float getSpeed() {
      return this.speed;
   }

   public void setOnAnimating(Runnable onAnimating) {
      this.onAnimating = onAnimating;
   }
}
