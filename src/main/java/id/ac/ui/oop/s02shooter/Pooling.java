package id.ac.ui.oop.s02shooter;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * SESSION 2 — the Object Pool pattern, and the two contracts it needs.
 *
 * A shooter fires hundreds of bullets a second. Calling `new Bullet()` for each
 * one produces hundreds of short-lived objects per second for the garbage
 * collector to clean up, and GC pauses show up as stutter. A pool recycles a
 * fixed set of objects instead: `obtain()` hands one out, `free()` takes it back.
 */
public final class Pooling {

    private Pooling() { }

    /** Contract 1: anything the pool creates. Session 2 also calls this Factory Method. */
    public interface Factory<T> {
        T create();
    }

    /**
     * Contract 2: anything the pool can recycle.
     * reset() must clear EVERY field. Forgetting one is the classic pool bug:
     * a recycled bullet arrives still carrying the last shot's state.
     */
    public interface Poolable {
        void reset();
    }

    /**
     * A generic pool. Note the bound: `T extends Poolable` is how the compiler
     * guarantees we can always reset what we recycle.
     */
    public static final class ObjectPool<T extends Poolable> {

        private final Deque<T> free = new ArrayDeque<>();
        private final Factory<T> factory;

        // Statistics — these exist so the HUD can make the lesson visible.
        private int created;
        private int reused;
        private int live;

        public ObjectPool(Factory<T> factory, int prefill) {
            this.factory = factory;
            for (int i = 0; i < prefill; i++) {
                free.push(factory.create());
                created++;
            }
        }

        /** Borrow an object: recycled if one is free, newly built only if not. */
        public T obtain() {
            live++;
            if (free.isEmpty()) {
                created++;
                return factory.create();
            }
            reused++;
            return free.pop();
        }

        /** Give it back. The object cleans itself; the pool just stores it. */
        public void free(T object) {
            object.reset();
            free.push(object);
            live--;
        }

        public int created() { return created; }
        public int reused()  { return reused; }
        public int live()    { return live; }
        public int idle()    { return free.size(); }
    }
}
