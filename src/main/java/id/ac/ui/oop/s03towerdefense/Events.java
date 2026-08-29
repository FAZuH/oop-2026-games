package id.ac.ui.oop.s03towerdefense;

import java.util.ArrayList;
import java.util.List;

/**
 * SESSION 3 — the Observer pattern.
 *
 * One enemy death has to pay gold, update the HUD, write a battle-log line and
 * (later) play a sound. Without Observer, Enemy would need a reference to all
 * four systems and would have to be edited every time a fifth appeared.
 *
 * With Observer, the enemy announces WHAT HAPPENED and does not care who is
 * listening. Adding a listener touches no existing class.
 */
public final class Events {

    private Events() { }

    public enum Kind { ENEMY_KILLED, ENEMY_LEAKED, TOWER_PLACED, TOWER_SOLD, GOLD_CHANGED }

    /** An immutable description of something that happened. */
    public record GameEvent(Kind kind, String detail, int amount) { }

    /** Anything that wants to hear about events. */
    public interface Listener {
        void onEvent(GameEvent event);
    }

    /** The subject. It holds listeners, not concrete systems. */
    public static final class EventBus {
        private final List<Listener> listeners = new ArrayList<>();

        public void subscribe(Listener listener)   { listeners.add(listener); }

        /**
         * The counterpart nobody remembers to call. A listener that is never
         * unsubscribed keeps being notified — and keeps being reachable, so the
         * garbage collector cannot free it. That is the "lapsed listener" leak.
         */
        public void unsubscribe(Listener listener) { listeners.remove(listener); }

        public void publish(GameEvent event) {
            // Copy first: a listener is allowed to subscribe/unsubscribe in response.
            for (Listener listener : new ArrayList<>(listeners)) {
                listener.onEvent(event);
            }
        }

        public int listenerCount() { return listeners.size(); }
    }
}
