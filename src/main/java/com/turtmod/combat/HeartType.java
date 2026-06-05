package com.turtmod.combat;

import net.minecraft.class_2960;

public enum HeartType {
   EMPTY(class_2960.method_60656("hud/heart/container")),
   RED_FULL(class_2960.method_60656("hud/heart/full")),
   RED_HALF(class_2960.method_60656("hud/heart/half")),
   YELLOW_FULL(class_2960.method_60656("hud/heart/absorbing_full")),
   YELLOW_HALF(class_2960.method_60656("hud/heart/absorbing_half"));

   public final class_2960 texture;

   private HeartType(class_2960 texture) {
      this.texture = texture;
   }

   // $FF: synthetic method
   private static HeartType[] $values() {
      return new HeartType[]{EMPTY, RED_FULL, RED_HALF, YELLOW_FULL, YELLOW_HALF};
   }
}
