package com.turtmod.mixin.client;

import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_465.class})
public interface HandledScreenMixinAccessor {
   @Accessor("field_2776")
   int turtmod$getX();

   @Accessor("field_2800")
   int turtmod$getY();

   @Accessor("field_2792")
   int turtmod$getBackgroundWidth();
}
