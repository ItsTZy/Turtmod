package com.turtmod.mixin.client;

import com.turtmod.utils.ShieldTracker;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_638;
import net.minecraft.class_7923;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_638.class})
public class ClientWorldMixin {
   @Inject(
      method = {"method_43207"},
      at = {@At("HEAD")}
   )
   private void turtmod$onPlaySoundAtPosition(double x, double y, double z, class_3414 sound, class_3419 category, float volume, float pitch, boolean useDistance, long seed, CallbackInfo ci) {
      class_2960 soundId = class_7923.field_41172.method_10221(sound);
      String sid = soundId != null ? soundId.toString() : null;
      if (sid != null && sid.contains("item.shield.break")) {
         ShieldTracker.handleBreakSound(x, y, z);
      }

   }
}
