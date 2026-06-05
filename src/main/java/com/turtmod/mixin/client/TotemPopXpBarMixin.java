package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.combat.PotionThrowTracker;
import com.turtmod.combat.TotemCounterFeature;
import com.turtmod.combat.TotemPopTracker;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_746;
import net.minecraft.class_9779;
import net.minecraft.class_11224;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_11224.class})
public class TotemPopXpBarMixin {
   @Inject(method = {"method_70865"}, at = {@At("RETURN")})
   private void turtmod$tintXpBar(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled) return;
      class_310 client = class_310.method_1551();
      if (client.field_1724 == null) return;

      int totemCount = config.combat.totemShowPopCounter
         ? TotemPopTracker.get(client.field_1724.method_5667())
         : TotemCounterFeature.countTotems(client.field_1724);
      
      int potCount = PotionThrowTracker.get(client.field_1724.method_5667());
      
      boolean totemColor = config.combat.totemColoredXpBar && totemCount > 0 && (totemCount <= 10 || config.combat.totemAlwaysShowXpBar);
      boolean potColor = config.combat.potionThrowColoredXpBar && potCount > 0 && (potCount <= 10 || config.combat.potionThrowAlwaysShowXpBar);

      if (!totemColor && !potColor) return;

      int color;
      if (totemColor && (!potColor || totemCount < potCount)) {
          color = config.combat.totemShowPopCounter ? TotemPopTracker.popColor(totemCount) : TotemCounterFeature.colorForCount(totemCount);
      } else {
          color = PotionThrowTracker.potsColor(potCount);
      }

      class_746 player = client.field_1724;
      int x = (client.method_22683().method_4486() - 182) / 2;
      int y = client.method_22683().method_4502() - 24 - 5;
      int progress = (int)(player.field_7510 * 182.0F);

      context.method_25294(x, y, x + 182, y + 5, 0x80000000);
      if (progress > 0) {
         context.method_25294(x, y, x + progress, y + 5, color);
      }
   }
}
