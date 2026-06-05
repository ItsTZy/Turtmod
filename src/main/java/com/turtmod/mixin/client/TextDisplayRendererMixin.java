package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.combat.PotionThrowTracker;
import com.turtmod.combat.TotemPopTracker;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_8113;
import net.minecraft.class_8138;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Appends totem-pop / potion-throw counts to nametags that use TextDisplay entities.
 * Targets the line-splitter in TextDisplayRenderer (class_8138$class_8141).
 */
@Mixin(targets = "net.minecraft.class_8138$class_8141")
public class TextDisplayRendererMixin {
    @ModifyVariable(
        method = "method_49057",
        at = @At("HEAD"),
        argsOnly = true,
        require = 0
    )
    private class_2561 turtmod$appendCountsToNametag(class_2561 text) {
        TurtModConfig config = TurtModClient.getConfig();
        class_310 client = class_310.method_1551();
        if (config == null || !config.misc.enabled || client == null || client.field_1687 == null) {
            return text;
        }

        boolean showTotem = config.combat.totemNametagPops;
        boolean showPots = config.combat.potionThrowNametagPots;
        if (!showTotem && !showPots) {
            return text;
        }

        String content = text.getString();
        if (content.isBlank()) {
            return text;
        }

        for (class_1657 player : client.field_1687.method_18456()) {
            String name = player.method_5820();
            int index = content.indexOf(name);
            if (index != -1 && !turtmod$isSurrounded(content, index, name.length())) {
                class_2561 result = text;
                if (showTotem) {
                    result = TotemPopTracker.appendPops(player, result);
                }
                if (showPots) {
                    result = PotionThrowTracker.appendPots(player, result);
                }
                return result;
            }
        }

        return text;
    }

    private boolean turtmod$isSurrounded(String text, int index, int length) {
        if (index > 0 && Character.isLetterOrDigit(text.charAt(index - 1))) {
            return true;
        }
        return index + length < text.length() && Character.isLetterOrDigit(text.charAt(index + length));
    }
}
