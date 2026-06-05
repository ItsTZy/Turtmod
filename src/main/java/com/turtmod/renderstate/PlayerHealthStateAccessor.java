package com.turtmod.renderstate;

import java.util.UUID;

public interface PlayerHealthStateAccessor {
   void turtmod$setHealth(float var1);

   float turtmod$getHealth();

   void turtmod$setMaxHealth(float var1);

   float turtmod$getMaxHealth();

   void turtmod$setAbsorption(float var1);

   float turtmod$getAbsorption();

   void turtmod$setLocalPlayer(boolean var1);

   boolean turtmod$isLocalPlayer();

   void turtmod$setUsingRiptide(boolean var1);

   boolean turtmod$isUsingRiptide();

   void turtmod$setUuid(UUID var1);

   UUID turtmod$getUuid();
}
