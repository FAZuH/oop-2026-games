package id.ac.ui.oop.s05breakout;

import javafx.scene.paint.Color;

import java.util.List;

/**
 * SESSION 5 — two concrete levels.
 *
 * Read these next to LevelLoader: neither class says anything about the ORDER
 * of the build. They only fill in steps. Adding a third level means adding one
 * class here and changing nothing else.
 */
public final class Levels {

    private Levels() { }

    /** A plain grid. No twist — it does not override the hook at all. */
    public static final class Classic extends LevelLoader {
        @Override protected String name() { return "Classic grid"; }

        @Override protected void layOutBricks(List<Brick> bricks) {
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 12; col++) {
                    bricks.add(new Brick(60 + col * (Brick.WIDTH + 6),
                                         70 + row * (Brick.HEIGHT + 6),
                                         1, Color.web("#0B5FA5")));
                }
            }
        }

        @Override protected double ballSpeed() { return 330; }
    }

    /** A pyramid, faster ball, AND it opts into the twist hook. */
    public static final class Pyramid extends LevelLoader {
        @Override protected String name() { return "Pyramid (armoured apex)"; }

        @Override protected void layOutBricks(List<Brick> bricks) {
            for (int row = 0; row < 5; row++) {
                int count = 11 - row * 2;
                double startX = (900 - count * (Brick.WIDTH + 6)) / 2.0;
                for (int col = 0; col < count; col++) {
                    bricks.add(new Brick(startX + col * (Brick.WIDTH + 6),
                                         70 + row * (Brick.HEIGHT + 6),
                                         1, Color.web("#7A4B9B")));
                }
            }
        }

        @Override protected double ballSpeed() { return 395; }

        /** This level DOES want the optional step. Classic leaves it alone. */
        @Override protected boolean hasTwist() { return true; }

        /**
         * The twist: crown the pyramid with a short row of armoured bricks that
         * take two hits. Visible on screen as the darker capstones.
         */
        @Override protected void applyTwist(List<Brick> bricks) {
            for (int col = 0; col < 3; col++) {
                bricks.add(new Brick(378 + col * (Brick.WIDTH + 6), 42, 2, Color.web("#B3261E")));
            }
        }
    }
}
