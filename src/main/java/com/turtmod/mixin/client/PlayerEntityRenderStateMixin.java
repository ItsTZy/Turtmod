package com.turtmod.mixin.client;

import net.minecraft.class_10055;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({class_10055.class})
public class PlayerEntityRenderStateMixin {
   @Unique
   public boolean turtmod$isLocalPlayer = false;
}
