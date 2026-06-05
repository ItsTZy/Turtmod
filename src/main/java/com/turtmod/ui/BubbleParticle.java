package com.turtmod.ui;

import java.util.Random;
import net.minecraft.class_332;

public class BubbleParticle {
   public float x;
   public float y;
   public float baseSpeed;
   public float size;
   public float vx;
   public float vy;
   public float alpha;
   public int color;

   public BubbleParticle(int w, int h, Random r) {
      this.reset(w, h, r);
      this.y = (float)r.nextInt(Math.max(1, h));
   }

   public void update(int mouseX, int mouseY, int w, int h, Random r) {
      this.y -= this.baseSpeed;
      float dx = this.x - (float)mouseX;
      float dy = this.y - (float)mouseY;
      float distSq = dx * dx + dy * dy;
      float range = 100.0F;
      if (distSq < range * range) {
         float dist = (float)Math.sqrt((double)distSq);
         if (dist < 1.0F) {
            dist = 1.0F;
         }

         float force = (range - dist) / range;
         this.vx += dx / dist * force * 3.5F;
         this.vy += dy / dist * force * 3.5F;
      }

      this.x += this.vx;
      this.y += this.vy;
      this.vx *= 0.85F;
      this.vy *= 0.85F;
      if (this.y < -10.0F) {
         this.reset(w, h, r);
      }

   }

   public void reset(int w, int h, Random r) {
      this.x = (float)r.nextInt(Math.max(1, w));
      this.y = (float)(h + 10);
      this.baseSpeed = 0.5F + r.nextFloat() * 1.5F;
      this.size = (float)(1 + r.nextInt(4));
      this.alpha = 0.3F + r.nextFloat() * 0.5F;
      this.vx = 0.0F;
      this.vy = 0.0F;
      this.color = r.nextBoolean() ? 4251856 : 16777215;
   }

   public void draw(class_332 context) {
      int argb = this.color | (int)(this.alpha * 0.4F * 255.0F) << 24;
      int s = (int)this.size;
      context.method_25294((int)this.x, (int)this.y, (int)this.x + s, (int)this.y + s, argb);
   }
}
