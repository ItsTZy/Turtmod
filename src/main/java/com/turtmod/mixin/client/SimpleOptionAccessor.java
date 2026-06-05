package com.turtmod.mixin.client;

import com.turtmod.mixin.SimpleOptionDuck;
import net.minecraft.class_7172;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_7172.class})
public interface SimpleOptionAccessor<T> extends SimpleOptionDuck<T> {
   @Accessor("field_37868")
   void turtmod$setValue(T var1);
}
