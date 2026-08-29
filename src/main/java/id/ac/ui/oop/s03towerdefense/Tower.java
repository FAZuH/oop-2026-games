package id.ac.ui.oop.s03towerdefense;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

/** SESSION 3 — a tower. Small, encapsulated, and unaware of the event bus. */
public class Tower {

    public static final int COST = 40;
    public static final int REFUND = 30;          // selling loses a little

    private final double x, y;
    private final double range = 108;
    private final double fireInterval = 0.55;
    private double cooldown;

    public Tower(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** Returns the enemy it shot this frame, or null. The caller publishes the event. */
    public Enemy update(double dt, List<Enemy> enemies) {
        cooldown -= dt;
        if (cooldown > 0) return null;
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && within(enemy)) {
                cooldown = fireInterval;
                enemy.damage(1);
                return enemy;
            }
        }
        return null;
    }

    private boolean within(Enemy enemy) {
        double dx = enemy.x() - x, dy = enemy.y() - y;
        return dx * dx + dy * dy <= range * range;
    }

    public void render(GraphicsContext g, boolean selected) {
        g.setStroke(Color.web("#0B5FA5", 0.35));
        g.setLineWidth(1);
        g.strokeOval(x - range, y - range, range * 2, range * 2);
        g.setFill(selected ? Color.web("#B3261E") : Color.web("#0B5FA5"));
        g.fillRoundRect(x - 13, y - 13, 26, 26, 5, 5);
    }

    public boolean contains(double px, double py) {
        return Math.abs(px - x) < 16 && Math.abs(py - y) < 16;
    }

    public double x() { return x; }
    public double y() { return y; }
}
