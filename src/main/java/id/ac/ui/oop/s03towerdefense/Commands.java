package id.ac.ui.oop.s03towerdefense;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * SESSION 3 — the Command pattern.
 *
 * A click could simply call world.placeTower(x, y). Making the action an OBJECT
 * instead buys three things you cannot get otherwise:
 *   - undo, because the object remembers what it did;
 *   - rebindable input, because a key can hold any Command;
 *   - a history you could log or replay.
 *
 * Tower defense is the honest home for this: "sell a tower, then change your
 * mind" is a real thing players want, not a contrived example.
 */
public final class Commands {

    private Commands() { }

    public interface Command {
        /** Do the thing. Returns false if it could not run (e.g. not enough gold). */
        boolean execute();

        /** Put the world back exactly as it was. */
        void undo();

        String describe();
    }

    /** Places a tower and remembers it, so undo can take it away again. */
    public static final class PlaceTowerCommand implements Command {
        private final World world;
        private final double x, y;
        private Tower placed;

        public PlaceTowerCommand(World world, double x, double y) {
            this.world = world; this.x = x; this.y = y;
        }

        @Override public boolean execute() {
            placed = world.tryPlaceTower(x, y);
            return placed != null;
        }

        @Override public void undo() {
            if (placed != null) world.removeTowerRefunding(placed);
        }

        @Override public String describe() {
            return String.format("place tower @ %.0f,%.0f", x, y);
        }
    }

    /** Sells a tower; undo re-buys it at the same spot. */
    public static final class SellTowerCommand implements Command {
        private final World world;
        private final Tower target;
        private boolean sold;

        public SellTowerCommand(World world, Tower target) {
            this.world = world; this.target = target;
        }

        @Override public boolean execute() {
            sold = world.trySellTower(target);
            return sold;
        }

        @Override public void undo() {
            if (sold) world.restoreTower(target);
        }

        @Override public String describe() {
            return String.format("sell tower @ %.0f,%.0f", target.x(), target.y());
        }
    }

    /**
     * The invoker. It runs commands and keeps the ones that succeeded, so the
     * last can be undone. It knows nothing about towers, gold or the world.
     */
    public static final class CommandHistory {
        private final Deque<Command> done = new ArrayDeque<>();

        public boolean run(Command command) {
            if (command.execute()) {
                done.push(command);
                return true;
            }
            return false;
        }

        public String undoLast() {
            if (done.isEmpty()) return null;
            Command command = done.pop();
            command.undo();
            return command.describe();
        }

        public int size() { return done.size(); }
    }
}
