package com.turtmod.mixin.client;

import net.minecraft.class_703;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read/write a particle's maxAge (class_703.field_3847) so "Fast Particles" can shorten its lifetime. */
@Mixin(class_703.class)
public interface ParticleAgeAccessor {
   @Accessor("field_3847")
   int turtmod$getMaxAge();

   @Accessor("field_3847")
   void turtmod$setMaxAge(int maxAge);
}
