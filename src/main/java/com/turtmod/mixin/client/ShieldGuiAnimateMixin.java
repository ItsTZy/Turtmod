package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import java.util.Map;
import net.minecraft.class_11228;
import net.minecraft.class_11245;
import net.minecraft.class_11540;
import net.minecraft.class_4587;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the shield's status recolour in sync in the GUI (hotbar / inventory item).
 *
 * GuiRenderer ({@code class_11228}) caches each drawn item's baked result in {@code renderedItems}
 * ({@code field_59913}, keyed by the item's model key) and reuses it on later frames. A shield baked
 * once therefore keeps whatever status colour it had — this is the "goes red and never comes back /
 * doesn't sync" bug. Here, for shields, we mark the item render state animated ({@code method_70947})
 * AND invalidate the cache entry's frame ({@code field_59930 = -1}) so the shield re-renders through
 * the full model path every frame, picking up the live status colour from {@link ShieldModelRendererMixin}.
 *
 * Port of Walksy ShieldStatus's pre-render-rewrite GuiRendererMixin (repos/ShieldStatus-1.21.9-1.21.10)
 * to verified 1.21.11 intermediary names. {@code method_71055} = the per-item lambda GuiRenderer runs
 * while preparing item elements; {@code class_11245} = ItemGuiElementRenderState, whose
 * {@code method_72121()} is the item's {@code class_11540} (KeyedItemRenderState) and
 * {@code method_72239()} is its model key.
 */
@Mixin(class_11228.class)
public abstract class ShieldGuiAnimateMixin {
   @Shadow
   @Final
   private Map<Object, Object> field_59913;

   @Inject(method = "method_71055", at = @At("HEAD"))
   private void turtmod$animateGuiShields(MutableBoolean hasOversized, int x, int y, MutableBoolean flag, class_4587 matrices, class_11245 itemElement, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled || !config.visual.shieldStatusRecolor) {
         return;
      }
      class_11540 itemRenderState = itemElement.method_72121();
      Object key = itemRenderState.method_72239();
      if (key == null || !key.toString().contains("shield")) {
         return;
      }
      Object renderedItem = this.field_59913.get(key);
      if (renderedItem != null) {
         itemRenderState.method_70947();
         ((RenderedItemAccessor) renderedItem).turtmod$setFrame(-1);
      }
   }
}
