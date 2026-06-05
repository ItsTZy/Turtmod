package com.turtmod.mixin;

import net.minecraft.class_1043;
import net.minecraft.class_4608;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_4608.class)
public interface OverlayTextureAccessor {
   @Accessor("field_21013")
   class_1043 turtmod$getTexture();
}
