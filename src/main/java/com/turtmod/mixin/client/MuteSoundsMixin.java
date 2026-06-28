package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_638;
import net.minecraft.class_6880;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mute-sounds toggles (ported from fireclient): cancels selected annoying sounds at the ClientWorld
 * playSound source. class_638 = ClientWorld; method_8486 = coordinate playSound (block sounds like
 * anvils/note blocks), method_8465 = entity playSound (totem pop, XP orb pickup). Sounds are matched
 * by their registry-id path so no brittle SoundEvent field constants are needed.
 */
@Mixin(class_638.class)
public class MuteSoundsMixin {

   @Inject(method = "method_8486(DDDLnet/minecraft/class_3414;Lnet/minecraft/class_3419;FFZ)V", at = @At("HEAD"), cancellable = true)
   private void turtmod$muteCoordSound(double x, double y, double z, class_3414 sound, class_3419 category, float volume, float pitch, boolean useDistance, CallbackInfo ci) {
      if (turtmod$isMuted(sound)) {
         ci.cancel();
      }
   }

   @Inject(method = "method_8465(Lnet/minecraft/class_1297;DDDLnet/minecraft/class_6880;Lnet/minecraft/class_3419;FFJ)V", at = @At("HEAD"), cancellable = true)
   private void turtmod$muteEntitySound(class_1297 entity, double x, double y, double z, class_6880<class_3414> sound, class_3419 category, float volume, float pitch, long seed, CallbackInfo ci) {
      if (sound != null && turtmod$isMuted(sound.comp_349())) {
         ci.cancel();
      }
   }

   private static boolean turtmod$isMuted(class_3414 sound) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg == null || !cfg.misc.enabled || !cfg.misc.muteSoundsEnabled || sound == null) {
         return false;
      }
      class_2960 id = sound.comp_3319();
      if (id == null) {
         return false;
      }
      String path = id.method_12832();
      if (cfg.misc.muteAnvil && path.startsWith("block.anvil")) {
         return true;
      }
      if (cfg.misc.muteNoteBlocks && path.startsWith("block.note_block")) {
         return true;
      }
      if (cfg.misc.muteTotemPop && path.equals("item.totem.use")) {
         return true;
      }
      if (cfg.misc.muteXpOrb && path.equals("entity.experience_orb.pickup")) {
         return true;
      }
      // Per-id mute list (managed via /turtmod sound), matched on the full "namespace:path" id.
      return !cfg.misc.mutedSoundIds.isEmpty() && cfg.misc.mutedSoundIds.contains(id.toString());
   }
}
