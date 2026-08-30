package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.class_310;
import net.minecraft.class_408;

/**
 * Command Keys: each bindable slot holds a macro (a sequence of messages, each with an optional
 * delay) edited via the Command Keys GUI. A leading "/" sends the line as a command, otherwise as
 * chat. Three modes:
 * <ul>
 *   <li><b>SEND</b> – fire the whole sequence once, honouring per-message delays.</li>
 *   <li><b>CYCLE</b> – each key press fires the next message in the list (wraps around).</li>
 *   <li><b>REPEAT</b> – toggle: keep looping the sequence until the key is pressed again.</li>
 * </ul>
 * Delays and looping are driven by {@link #tick}, called once per client tick.
 */
public final class CommandKeysFeature {
   public static final int SLOTS = 8;

   private CommandKeysFeature() {
   }

   /** A message scheduled to be sent in {@code ticksLeft} ticks. */
   private static final class Pending {
      int slot;
      int ticksLeft;
      String text;
      boolean typeInChat;
   }

   private static final List<Pending> PENDING = new ArrayList<>();
   private static final int[] CYCLE_INDEX = new int[SLOTS];
   private static final boolean[] REPEAT_ACTIVE = new boolean[SLOTS];
   // Min spacing between loops in REPEAT mode so a zero-delay macro doesn't spam every tick.
   private static final int REPEAT_MIN_GAP = 10;

   /** Called when a slot's keybind is pressed. */
   public static void trigger(class_310 client, TurtModConfig config, int slot) {
      if (!active(client, config) || slot < 0 || slot >= SLOTS) {
         return;
      }
      TurtModConfig.CommandKey key = macro(config, slot);
      if (key == null) {
         return;
      }
      List<TurtModConfig.CmdMsg> msgs = nonEmpty(key.messages);
      String mode = key.mode == null ? "SEND" : key.mode;

      switch (mode) {
         case "CYCLE" -> {
            if (!msgs.isEmpty()) {
               int idx = Math.floorMod(CYCLE_INDEX[slot], msgs.size());
               CYCLE_INDEX[slot] = idx + 1;
               sendNow(client, msgs.get(idx).text, key.typeInChat);
            }
         }
         case "REPEAT" -> {
            REPEAT_ACTIVE[slot] = !REPEAT_ACTIVE[slot];
            clearPending(slot);
            if (REPEAT_ACTIVE[slot]) {
               enqueue(slot, key, msgs, 0);
            }
         }
         default -> { // SEND
            clearPending(slot);
            enqueue(slot, key, msgs, 0);
         }
      }
   }

   /** Advances pending sends and re-arms active REPEAT loops. */
   public static void tick(class_310 client, TurtModConfig config) {
      if (!active(client, config)) {
         PENDING.clear();
         java.util.Arrays.fill(REPEAT_ACTIVE, false);
         return;
      }
      Iterator<Pending> it = PENDING.iterator();
      while (it.hasNext()) {
         Pending p = it.next();
         if (--p.ticksLeft <= 0) {
            sendNow(client, p.text, p.typeInChat);
            it.remove();
         }
      }
      for (int slot = 0; slot < SLOTS; slot++) {
         if (REPEAT_ACTIVE[slot] && !hasPending(slot)) {
            TurtModConfig.CommandKey key = macro(config, slot);
            List<TurtModConfig.CmdMsg> msgs = key == null ? List.of() : nonEmpty(key.messages);
            if (msgs.isEmpty()) {
               REPEAT_ACTIVE[slot] = false;
            } else {
               enqueue(slot, key, msgs, REPEAT_MIN_GAP);
            }
         }
      }
   }

   /** Schedules every (non-empty) message of a macro, starting {@code leadTicks} from now. */
   private static void enqueue(int slot, TurtModConfig.CommandKey key, List<TurtModConfig.CmdMsg> msgs, int leadTicks) {
      int t = Math.max(0, leadTicks);
      for (TurtModConfig.CmdMsg m : msgs) {
         t += Math.max(0, m.delay);
         Pending p = new Pending();
         p.slot = slot;
         p.ticksLeft = Math.max(1, t);
         p.text = m.text.trim();
         p.typeInChat = key.typeInChat;
         PENDING.add(p);
      }
   }

   private static void sendNow(class_310 client, String text, boolean typeInChat) {
      if (client.field_1724 == null || client.field_1724.field_3944 == null || text == null) {
         return;
      }
      String t = text.trim();
      if (t.isEmpty()) {
         return;
      }
      if (typeInChat) {
         // Open the chat box pre-filled with the text instead of sending it.
         client.method_1507(new class_408(t, false));
         return;
      }
      if (t.startsWith("/")) {
         // method_45730 = send command (no leading slash); method_45729 = send chat message.
         client.field_1724.field_3944.method_45730(t.substring(1));
      } else {
         client.field_1724.field_3944.method_45729(t);
      }
   }

   private static boolean active(class_310 client, TurtModConfig config) {
      return client != null && client.field_1724 != null && config != null
         && config.misc.enabled && config.misc.commandKeysEnabled;
   }

   private static TurtModConfig.CommandKey macro(TurtModConfig config, int slot) {
      TurtModConfig.CommandKey[] all = config.misc.commandKeyMacros;
      return all != null && slot >= 0 && slot < all.length ? all[slot] : null;
   }

   private static List<TurtModConfig.CmdMsg> nonEmpty(List<TurtModConfig.CmdMsg> messages) {
      List<TurtModConfig.CmdMsg> out = new ArrayList<>();
      if (messages != null) {
         for (TurtModConfig.CmdMsg m : messages) {
            if (m != null && m.text != null && !m.text.trim().isEmpty()) {
               out.add(m);
            }
         }
      }
      return out;
   }

   private static void clearPending(int slot) {
      PENDING.removeIf(p -> p.slot == slot);
   }

   private static boolean hasPending(int slot) {
      for (Pending p : PENDING) {
         if (p.slot == slot) {
            return true;
         }
      }
      return false;
   }
}
