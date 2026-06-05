package com.turtmod.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;

/**
 * Tracks shield usable / disabled state per player. Faithful port of Walksy's
 * {@code WalksyLibShieldStateManager} (repos/WalksyLib-26.1), which is what the current
 * ShieldStatus mod actually uses.
 *
 * Why this rewrite: the previous turtmod version wrote into each OTHER player's REAL
 * {@code class_1796} item-cooldown manager and matched a shield-break sound to any nearby
 * player we had attacked. With no self-vs-other disambiguation, the LOCAL player's own
 * shield-break sound could land near another player and turn THEIR shield red ("red at the
 * wrong time"). The reference solves this with:
 *   - a SEPARATE internal cooldown manager (UUID -> countdown ticks) for non-local players
 *     (the local player still reads the server-synced vanilla cooldown);
 *   - a break-sound matcher that only credits another player if they are CLOSER to the sound
 *     than we are, otherwise it defers one tick ({@code pendingBreak}) and only disables the
 *     other player if OUR shield did not go on cooldown in the meantime.
 */
public final class ShieldTracker {
   private static final int DISABLE_TICKS = 100;
   private static final long ATTACK_ENTRY_TTL_MS = 1000L;
   private static final byte SHIELD_DISABLE_STATUS = 30;
   private static final double MATCH_RADIUS_SQ = 36.0; // 6 blocks (matches reference)
   private static final class_310 client = class_310.method_1551();
   private static final class_1799 SHIELD_STACK = new class_1799(class_1802.field_8255);

   private static final Map<UUID, Integer> shieldUseTicks = new HashMap<>();
   private static final Map<UUID, AttackEntry> attackedPlayerEntries = new HashMap<>();
   private static final ShieldCooldownManager cooldownManager = new ShieldCooldownManager();
   private static int localShieldCooldownTicks = 0;
   private static int currentTick = 0;
   private static PendingBreak pendingBreak = null;

   private ShieldTracker() {
   }

   private static void disable(UUID playerId) {
      if (playerId != null) {
         cooldownManager.setCooldown(playerId, DISABLE_TICKS);
      }
   }

   /** Pre-1.21.5 servers broadcast shield disable via entity status 30. Never for the local
    *  player (the local player uses the server-synced vanilla cooldown instead). */
   public static void handleEntityStatus(class_1657 player, byte status) {
      if (player != null && status == SHIELD_DISABLE_STATUS && client != null && player != client.field_1724) {
         disable(player.method_5667());
      }
   }

   /** Post-1.21.4 servers signal a shield disable with a shield-break SOUND. This is the
    *  delicate part: attribute it to the right player without false-flagging a bystander. */
   public static void handleBreakSound(double x, double y, double z) {
      if (client == null || client.field_1687 == null || client.field_1724 == null) {
         return;
      }
      class_1657 local = client.field_1724;
      long now = System.currentTimeMillis();

      // If OUR shield is already on cooldown, this fresh break sound cannot be ours -> credit
      // the nearest other player to the sound.
      if (localShieldCooldownTicks > 2) {
         class_1657 nearest = nearestPlayerToSound(x, y, z, local);
         if (nearest != null) {
            disable(nearest.method_5667());
         }
         return;
      }

      class_1657 best = null;
      double bestDistSq = Double.POSITIVE_INFINITY;
      for (Map.Entry<UUID, AttackEntry> e : attackedPlayerEntries.entrySet()) {
         AttackEntry ae = e.getValue();
         if (now - ae.time > ATTACK_ENTRY_TTL_MS || !ae.wasBlocking) {
            continue;
         }
         class_1657 candidate = playerById(e.getKey());
         if (candidate == null || candidate == local) {
            continue;
         }
         double dx = candidate.method_23317() - x;
         double dy = candidate.method_23318() - y;
         double dz = candidate.method_23321() - z;
         double currentDistSq = dx * dx + dy * dy + dz * dz;

         double sdx = ae.targetX - x;
         double sdy = ae.targetY - y;
         double sdz = ae.targetZ - z;
         double storedDistSq = sdx * sdx + sdy * sdy + sdz * sdz;

         double effectiveDistSq = Math.min(currentDistSq, storedDistSq);
         if (effectiveDistSq > MATCH_RADIUS_SQ) {
            continue;
         }
         if (effectiveDistSq < bestDistSq) {
            bestDistSq = effectiveDistSq;
            best = candidate;
         }
      }

      double selfDx = local.method_23317() - x;
      double selfDy = local.method_23318() - y;
      double selfDz = local.method_23321() - z;
      double selfDistSq = selfDx * selfDx + selfDy * selfDy + selfDz * selfDz;

      // Only credit the other player if the sound is CLOSER to them than to us.
      if (best != null && bestDistSq < selfDistSq) {
         disable(best.method_5667());
         attackedPlayerEntries.remove(best.method_5667());
         return;
      }
      if (best == null) {
         return;
      }
      // Ambiguous (we are at least as close): defer one tick. If OUR shield doesn't go on
      // cooldown by then, the break was the other player's after all.
      attackedPlayerEntries.remove(best.method_5667());
      pendingBreak = new PendingBreak(best.method_5667(), currentTick);
   }

   private static class_1657 nearestPlayerToSound(double x, double y, double z, class_1657 local) {
      class_1657 nearest = null;
      double nearestDistSq = Double.POSITIVE_INFINITY;
      for (class_1657 player : client.field_1687.method_18456()) {
         if (player == local) {
            continue;
         }
         double dx = player.method_23317() - x;
         double dy = player.method_23318() - y;
         double dz = player.method_23321() - z;
         double distSq = dx * dx + dy * dy + dz * dz;
         if (distSq < nearestDistSq) {
            nearestDistSq = distSq;
            nearest = player;
         }
      }
      return nearest;
   }

   /** Called when the local player attacks an entity. Arms a later break-confirmation for any
    *  target we hit with an axe; the actual disable is gated by the break-sound matcher. */
   public static void handlePlayerAttack(class_1657 target) {
      if (client == null || client.field_1724 == null || target == null) {
         return;
      }
      if (disablesShield(client.field_1724)) {
         boolean estBlocking = shieldUseTicks.getOrDefault(target.method_5667(), 0) >= 3;
         attackedPlayerEntries.put(target.method_5667(),
            new AttackEntry(target.method_23317(), target.method_23318(), target.method_23321(),
               System.currentTimeMillis(), estBlocking));
      }
   }

   public static boolean isCoolingDown(class_1657 player) {
      if (player == null) {
         return false;
      }
      if (client != null && player == client.field_1724) {
         return player.method_7357().method_7904(SHIELD_STACK);
      }
      return cooldownManager.isCoolingDown(player.method_5667());
   }

   /** Recovery fraction: 0 right after the shield breaks -> 1 when fully recovered. The shield
    *  renderer interpolates broken->usable using this, so the convention must match. */
   public static float getCooldownProgress(class_1657 player) {
      if (player == null) {
         return 0.0F;
      }
      if (client != null && player == client.field_1724) {
         return Math.max(0.0F, Math.min(1.0F, 1.0F - player.method_7357().method_7905(SHIELD_STACK, 0.0F)));
      }
      int remaining = cooldownManager.getRemainingTicks(player.method_5667());
      if (remaining <= 0) {
         return 0.0F;
      }
      return Math.max(0.0F, Math.min(1.0F, 1.0F - (float)remaining / (float)DISABLE_TICKS));
   }

   public static void tick() {
      if (client == null || client.field_1687 == null) {
         return;
      }
      long now = System.currentTimeMillis();
      currentTick++;

      for (class_1657 player : client.field_1687.method_18456()) {
         UUID id = player.method_5667();
         if (isHoldingUsableShield(player) && player.method_6115()) {
            shieldUseTicks.put(id, shieldUseTicks.getOrDefault(id, 0) + 1);
         } else {
            shieldUseTicks.put(id, 0);
         }
      }

      if (client.field_1724 != null && client.field_1724.method_7357().method_7904(SHIELD_STACK)) {
         localShieldCooldownTicks++;
      } else {
         localShieldCooldownTicks = 0;
      }

      // Resolve a deferred (ambiguous) break: if OUR shield didn't go on cooldown, it was theirs.
      if (pendingBreak != null && currentTick - pendingBreak.createdTick >= 1) {
         if (client.field_1724 == null || !client.field_1724.method_7357().method_7904(SHIELD_STACK)) {
            disable(pendingBreak.targetId);
         }
         pendingBreak = null;
      }

      attackedPlayerEntries.entrySet().removeIf((e) -> now - e.getValue().time > ATTACK_ENTRY_TTL_MS);
      cooldownManager.tick();
   }

   public static boolean isUsingShield(class_1657 player) {
      return player != null && shieldUseTicks.getOrDefault(player.method_5667(), 0) >= 5;
   }

   public static boolean isHoldingUsableShield(class_1657 entity) {
      return (entity.method_6047().method_31574(class_1802.field_8255) || entity.method_6079().method_31574(class_1802.field_8255)) && !isHoldingAnimationItemMainHand(entity);
   }

   private static boolean isHoldingAnimationItemMainHand(class_1657 entity) {
      return entity.method_6047().method_7935(entity) != 0 && !entity.method_6079().method_31574(class_1802.field_8255);
   }

   public static boolean disablesShield(class_1657 player) {
      return player.method_59958().method_7909() instanceof class_1743;
   }

   private static class_1657 playerById(UUID id) {
      if (client == null || client.field_1687 == null || id == null) {
         return null;
      }
      for (class_1657 player : client.field_1687.method_18456()) {
         if (player.method_5667().equals(id)) {
            return player;
         }
      }
      return null;
   }

   private record AttackEntry(double targetX, double targetY, double targetZ, long time, boolean wasBlocking) {
   }

   private record PendingBreak(UUID targetId, int createdTick) {
   }

   /** Internal, client-side cooldown timer for OTHER players (the local player uses the real,
    *  server-synced vanilla cooldown manager instead). */
   private static final class ShieldCooldownManager {
      private final Map<UUID, Integer> cooldowns = new ConcurrentHashMap<>();

      void setCooldown(UUID id, int ticks) {
         if (id != null) {
            cooldowns.put(id, Math.max(0, ticks));
         }
      }

      boolean isCoolingDown(UUID id) {
         Integer t = id == null ? null : cooldowns.get(id);
         return t != null && t > 0;
      }

      int getRemainingTicks(UUID id) {
         Integer t = id == null ? null : cooldowns.get(id);
         return t == null ? 0 : Math.max(0, t);
      }

      void tick() {
         Iterator<Map.Entry<UUID, Integer>> it = cooldowns.entrySet().iterator();
         while (it.hasNext()) {
            Map.Entry<UUID, Integer> e = it.next();
            int t = e.getValue() - 1;
            if (t <= 0) {
               it.remove();
            } else {
               e.setValue(t);
            }
         }
      }
   }
}
