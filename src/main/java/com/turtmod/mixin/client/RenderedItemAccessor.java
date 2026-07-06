package com.turtmod.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the package-private {@code frame} field ({@code field_59930}) on GuiRenderer's cached
 * {@code RenderedItem} ({@code class_11228$class_11229}) so {@link ShieldGuiAnimateMixin} can
 * invalidate a cached GUI shield (set frame = -1) and force it to re-render each frame. Without this
 * accessor the field can't be written from a mixin in another package.
 */
@Mixin(targets = "net.minecraft.class_11228$class_11229")
public interface RenderedItemAccessor {
   @Accessor("field_59930")
   void turtmod$setFrame(int frame);
}
