package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.hud.CustomThemeRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1923;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_340;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_340.class})
public abstract class DebugHudMixin {
   @Shadow
   @Final
   private class_310 field_2079;
   @Shadow
   @Final
   private class_327 field_2081;

   @Shadow
   public abstract boolean method_53536();

   @Inject(
      method = {"method_1846"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void turtmod$cleanF3(class_332 context, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      // Gate on method_72776() — the actual F3 debug *text screen* toggle — NOT method_53536(),
      // which also returns true whenever a debug *overlay* (F3+B hitboxes, F3+G chunk borders…)
      // is active. Using method_53536() made Clean F3 pop up the moment you pressed F3+B, and kept
      // it stuck on while hitboxes were enabled (a plain F3 couldn't dismiss it). Tying it to the
      // text-screen toggle makes F3 behave normally and keeps hitboxes independent.
      if (config != null && config.misc.enabled && config.hud.cleanF3Mode && this.field_2079.field_1724 != null && !this.field_2079.field_1690.field_1842 && this.field_2079.field_61504.method_72776()) {
         this.renderCleanF3(context, config);
         ci.cancel();
      }
   }

   private void renderCleanF3(class_332 context, TurtModConfig config) {
      int fps = this.field_2079.method_47599();
      int ping = -1;
      if (this.field_2079.method_1562() != null) {
         class_640 entry = this.field_2079.method_1562().method_2871(this.field_2079.field_1724.method_5667());
         if (entry != null) {
            ping = entry.method_2959();
         }
      }

      int x = config.hud.cleanF3X;
      int y = config.hud.cleanF3Y;
      class_2338 pos = this.field_2079.field_1724.method_24515();
      class_1923 chunkPos = new class_1923(pos);
      class_243 playerPos = new class_243(this.field_2079.field_1724.method_23317(), this.field_2079.field_1724.method_23318(), this.field_2079.field_1724.method_23321());
      List<String> lines = new ArrayList();
      if (config.hud.cleanF3ShowFpsPing) {
         lines.add("FPS: " + fps + " | Ping: " + (ping >= 0 ? ping + "ms" : "--"));
      }

      if (config.hud.cleanF3ShowPosition) {
         lines.add(String.format("XYZ: %.3f / %.3f / %.3f", playerPos.field_1352, playerPos.field_1351, playerPos.field_1350));
         lines.add(String.format("Block: %d %d %d", pos.method_10263(), pos.method_10264(), pos.method_10260()));
      }

      if (config.hud.cleanF3ShowChunk) {
         lines.add(String.format("Chunk: %d %d | Local: %d %d %d", chunkPos.field_9181, chunkPos.field_9180, pos.method_10263() & 15, pos.method_10264() & 15, pos.method_10260() & 15));
      }

      if (config.hud.cleanF3ShowLight && this.field_2079.field_1687 != null) {
         int blockLight = this.field_2079.field_1687.method_8314(class_1944.field_9282, pos);
         int skyLight = this.field_2079.field_1687.method_8314(class_1944.field_9284, pos);
         lines.add(String.format("Light: %d block, %d sky", blockLight, skyLight));
      }

      if (config.hud.cleanF3ShowFacing) {
         lines.add(String.format("Facing: %s (%.1f/%.1f)", this.field_2079.field_1724.method_5735().method_15434().toUpperCase(), this.field_2079.field_1724.method_36454(), this.field_2079.field_1724.method_36455()));
      }

      if (config.hud.cleanF3ShowSpeed) {
         double dx = this.field_2079.field_1724.method_23317() - this.field_2079.field_1724.field_6014;
         double dz = this.field_2079.field_1724.method_23321() - this.field_2079.field_1724.field_5969;
         double speed = Math.sqrt(dx * dx + dz * dz) * 20.0;
         lines.add(String.format("Speed: %.2f m/s", speed));
      }

      if (config.hud.cleanF3ShowBiome && this.field_2079.field_1687 != null) {
         Optional var10001 = this.field_2079.field_1687.method_23753(pos).method_40230().map((key) -> key.method_29177().toString());
         lines.add("Biome: " + (String)var10001.orElse("unknown"));
      }

      if (config.hud.cleanF3ShowDayTime && this.field_2079.field_1687 != null) {
         long dayTime = this.field_2079.field_1687.method_8532() % 24000L;
         long day = this.field_2079.field_1687.method_8532() / 24000L;
         int hour = (int)((dayTime / 1000L + 6L) % 24L);
         int minute = (int)((dayTime % 1000L) * 60L / 1000L);
         lines.add(String.format("Day %d | %02d:%02d", day, hour, minute));
      }

      if (config.hud.cleanF3ShowHeldItem) {
         net.minecraft.class_1799 held = this.field_2079.field_1724.method_6047();
         if (!held.method_7960()) {
            lines.add("Held: " + held.method_7964().getString() + (held.method_7947() > 1 ? " x" + held.method_7947() : ""));
         }
      }

      if (config.hud.cleanF3ShowMemory) {
         long max = Runtime.getRuntime().maxMemory();
         long total = Runtime.getRuntime().totalMemory();
         long free = Runtime.getRuntime().freeMemory();
         long used = total - free;
         lines.add(String.format("Mem: %d%% %dMB/%dMB", used * 100L / max, used / 1048576L, max / 1048576L));
      }

      if (config.hud.cleanF3ShowLookingAt) {
         class_239 hitResult = this.field_2079.field_1765;
         if (hitResult != null) {
            if (hitResult instanceof class_3965) {
               class_3965 blockHit = (class_3965)hitResult;
               class_2338 lookedPos = blockHit.method_17777();
               lines.add(String.format("Looking at block: %d %d %d", lookedPos.method_10263(), lookedPos.method_10264(), lookedPos.method_10260()));
            } else if (hitResult instanceof class_3966) {
               class_3966 entityHit = (class_3966)hitResult;
               lines.add("Looking at entity: " + entityHit.method_17782().method_5477().getString());
            }
         }
      }

      if (!lines.isEmpty()) {
         int boxWidth = 0;

         for(String line : lines) {
            boxWidth = Math.max(boxWidth, this.field_2081.method_1727(line));
         }

         int boxHeight = lines.size() * 10 + 8;
         float scale = CustomThemeRenderer.getHudScale(config, config.hud.cleanF3ScalePercent);
         context.method_51448().pushMatrix();
         context.method_51448().translate((float)x, (float)y);
         context.method_51448().scale(scale, scale);
         context.method_51448().translate((float)(-x), (float)(-y));
         if (config.hud.cleanF3ShowBackground) {
            CustomThemeRenderer.renderThemedBox(context, x, y, boxWidth + 10, boxHeight, config);
         }

         for(int i = 0; i < lines.size(); ++i) {
            int color = i == 0 ? CustomThemeRenderer.getTextColor(config) : CustomThemeRenderer.getMutedTextColor(config);
            context.method_27535(this.field_2081, class_2561.method_43470((String)lines.get(i)), x + 5, y + 4 + i * 10, color);
         }

         context.method_51448().popMatrix();
      }
   }
}
