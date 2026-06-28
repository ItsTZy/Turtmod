package com.turtmod.mixin.client;

import net.minecraft.class_332;
import net.minecraft.class_355;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes the private renderLatencyIcon (class_355.method_1923) so the ping redirect can still
 * draw the vanilla latency bars after drawing the numeric ping. class_355 = PlayerTabOverlay.
 */
@Mixin({class_355.class})
public interface PlayerListPingInvoker {
   @Invoker("method_1923")
   void turtmod$renderLatencyIcon(class_332 context, int width, int x, int y, class_640 entry);
}
