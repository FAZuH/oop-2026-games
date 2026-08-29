package id.ac.ui.oop.s03towerdefense;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * SESSION 3 — an enemy walking a FIXED waypoint path.
 *
 * There is no pathfinding anywhere in this game and there never will be: the
 * route is a list of points, and the enemy walks from one to the next. That is
 * the single decision that keeps a tower-defense game buildable in one session.
 */
public class Enemy {

    private final double[][] path;
    private int leg;              // which segment of the path we are on
    private double x, y;
    private final double speed;
    private int health;
    private final int maxHealth;
    private boolean reachedEnd;

    public Enemy(double[][] path, double speed, int health) {
        this.path = path;
        this.x = path[0][0];
        this.y = path[0][1];
        this.speed = speed;
        this.health = health;
        this.maxHealth = health;
    }

    public void update(double dt) {
        if (reachedEnd || !isAlive()) return;
        if (leg >= path.length - 1) { reachedEnd = true; return; }

        double tx = path[leg + 1][0], ty = path[leg + 1][1];
        double dx = tx - x, dy = ty - y;
        double distance = Math.hypot(dx, dy);
        double step = speed * dt;                 // dt again: same speed everywhere

        if (step >= distance) {
            x = tx; y = ty; leg++;                // waypoint reached, aim at the next
        } else {
            x += dx / distance * step;
            y += dy / distance * step;
        }
    }

    public void render(GraphicsContext g) {
        g.setFill(Color.web("#10233A"));
        g.fillOval(x - 9, y - 9, 18, 18);
        g.setFill(Color.web("#CFDDE6"));
        g.fillRect(x - 11, y - 17, 22, 4);
        g.setFill(Color.web("#2E7D32"));
        g.fillRect(x - 11, y - 17, 22.0 * health / maxHealth, 4);
    }

    public void damage(int amount) { health -= amount; }
    public boolean isAlive()       { return health > 0; }
    public boolean reachedEnd()    { return reachedEnd; }
    public double x()              { return x; }
    public double y()              { return y; }
}
