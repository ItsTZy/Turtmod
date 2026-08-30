package com.turtmod.mixin.client;

import net.minecraft.class_315;
import net.minecraft.class_7172;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes GameOptions' private toggle-sprint option (field_21333 = the "toggleSprint" control
 * setting) so the Sprint Display can show "Toggled" vs "Held" based on the player's actual control
 * mode instead of a runtime heuristic. class_315 = GameOptions.
 */
@Mixin(class_315.class)
public interface GameOptionsAccessor {
   @Accessor("field_21333")
   class_7172<Boolean> turtmod$getToggleSprint();
}
