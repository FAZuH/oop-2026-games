package id.ac.ui.oop.s05breakout;

import java.util.ArrayList;
import java.util.List;

/**
 * SESSION 5 — the Template Method pattern.
 *
 * Every level is built the same way: clear the field, lay out the bricks, set
 * the ball speed, then (optionally) apply a twist. The ORDER is fixed and must
 * not vary — so {@link #load()} is final. Subclasses fill in the steps, they do
 * not get to rearrange them.
 *
 * "Don't call us, we'll call you" — the parent class is in charge. That is the
 * Hollywood Principle, and it is inversion of control in its smallest form.
 */
public abstract class LevelLoader {

    /**
     * THE TEMPLATE METHOD. final = the algorithm's shape is not negotiable.
     */
    public final Level load() {
        List<Brick> bricks = new ArrayList<>();
        layOutBricks(bricks);                 // step 1 — every level must
        double speed = ballSpeed();           // step 2 — every level must
        if (hasTwist()) {                     // step 3 — optional HOOK
            applyTwist(bricks);
        }
        return new Level(name(), bricks, speed);
    }

    // ---- steps every subclass MUST supply -------------------------------
    protected abstract String name();
    protected abstract void layOutBricks(List<Brick> bricks);
    protected abstract double ballSpeed();

    // ---- hooks a subclass MAY override; sensible defaults here ----------
    /** Default: no twist. Override and return true to opt in. */
    protected boolean hasTwist() { return false; }
    protected void applyTwist(List<Brick> bricks) { /* deliberately empty */ }

    /** What load() produces. A plain value object. */
    public record Level(String name, List<Brick> bricks, double ballSpeed) { }
}
