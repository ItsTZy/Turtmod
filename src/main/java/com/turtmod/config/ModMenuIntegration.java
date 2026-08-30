package com.turtmod.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.class_437;

public final class ModMenuIntegration implements ModMenuApi {
   public ConfigScreenFactory<class_437> getModConfigScreenFactory() {
      return TurtModMainMenuScreen::new;
   }
}
