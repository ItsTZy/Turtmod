package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.class_10072;
import net.minecraft.class_1297;
import net.minecraft.class_1684;
import net.minecraft.class_310;
import net.minecraft.class_3417;
import net.minecraft.class_953;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Pearl Detector (ported from repos/PearlDetector-main). Plays a one-shot alert sound the
 * first time an ender pearl appears within a configurable distance window of the local
 * player — useful for spotting enemy pearls in PvP. Hooks the flying-item renderer's
 * render-state extraction (method_62548), which is where the concrete entity is available
 * in 1.21.11's render-state pipeline.
 */
@Mixin({class_953.class})
public abstract class FlyingItemEntityRendererMixin {
   @Unique
   private final Set<Integer> turtmod$notifiedPearls = new HashSet();

   @Inject(
      method = {"method_62548"},
      at = {@At("HEAD")},
      require = 0
   )
   private void turtmod$pearlAlert(class_1297 entity, class_10072 state, float tickDelta, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      class_310 client = class_310.method_1551();
      if (config == null || !config.misc.enabled || !config.visual.pearlDetectorEnabled || !config.visual.pearlPlaySound) {
         return;
      }
      if (client == null || client.field_1724 == null || !(entity instanceof class_1684)) {
         return;
      }
      if (entity.field_6012 >= 2) {
         return;
      }
      int id = entity.method_5628();
      if (this.turtmod$notifiedPearls.contains(id)) {
         return;
      }
      double distSq = entity.method_5858(client.field_1724);
      double min = config.visual.pearlMinDistance;
      double max = config.visual.pearlMaxDistance;
      if (distSq >= min * min && distSq <= max * max) {
         client.field_1724.method_5783(class_3417.field_14879, config.visual.pearlSoundVolume, config.visual.pearlSoundPitch);
         this.turtmod$notifiedPearls.add(id);
      }
   }
}
