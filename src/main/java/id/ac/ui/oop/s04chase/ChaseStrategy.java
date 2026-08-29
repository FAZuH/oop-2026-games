package id.ac.ui.oop.s04chase;

/**
 * SESSION 4 — the Strategy pattern.
 *
 * Three ways to hunt the player. Each is a whole algorithm in its own class,
 * and a hunter can be handed a different one at RUNTIME. Without Strategy this
 * would be a switch inside Hunter.update() that grows every time someone
 * invents a new behaviour.
 *
 * This is composition beating inheritance: we do not need DirectHunter,
 * PredictiveHunter and FlankingHunter subclasses. One Hunter class HAS-A
 * strategy, and swapping the strategy changes what it does.
 */
public interface ChaseStrategy {

    /**
     * Returns the point the hunter should steer toward this frame.
     * @return a two-element array {x, y}
     */
    double[] aimFor(double hunterX, double hunterY,
                    double playerX, double playerY,
                    double playerVX, double playerVY);

    String name();

    // ------------------------------------------------------------ variants

    /** Head straight at where the player is now. Easy to dodge. */
    final class Direct implements ChaseStrategy {
        @Override public double[] aimFor(double hx, double hy, double px, double py,
                                         double pvx, double pvy) {
            return new double[]{px, py};
        }
        @Override public String name() { return "Direct — aims where you ARE"; }
    }

    /** Lead the target: aim where the player is about to be. Noticeably harder. */
    final class Predictive implements ChaseStrategy {
        private final double lookAhead = 0.55;   // seconds
        @Override public double[] aimFor(double hx, double hy, double px, double py,
                                         double pvx, double pvy) {
            return new double[]{px + pvx * lookAhead, py + pvy * lookAhead};
        }
        @Override public String name() { return "Predictive — aims where you WILL BE"; }
    }

    /** Approach from the side rather than head-on, so it cuts corners off. */
    final class Flanking implements ChaseStrategy {
        @Override public double[] aimFor(double hx, double hy, double px, double py,
                                         double pvx, double pvy) {
            double dx = px - hx, dy = py - hy;
            double distance = Math.hypot(dx, dy);
            if (distance < 1) return new double[]{px, py};
            // offset perpendicular to the approach, shrinking as it closes in
            double side = Math.min(distance * 0.6, 150);
            return new double[]{px - dy / distance * side, py + dx / distance * side};
        }
        @Override public String name() { return "Flanking — comes at you from the side"; }
    }
}
