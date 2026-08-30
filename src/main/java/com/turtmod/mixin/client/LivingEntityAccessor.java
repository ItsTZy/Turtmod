package com.turtmod.mixin.client;

import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_1309.class})
public interface LivingEntityAccessor {
   @Accessor("field_6235")
   int turtmod$getHurtTime();
}
