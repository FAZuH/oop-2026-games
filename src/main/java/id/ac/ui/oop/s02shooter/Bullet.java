package id.ac.ui.oop.s02shooter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * SESSION 2 — a poolable object.
 *
 * Note there is no constructor argument: a pooled object is built empty and
 * configured by {@link #fire}. Its {@link #reset} must clear every field, or a
 * recycled bullet turns up still carrying the previous shot's position.
 */
public class Bullet implements Pooling.Poolable {

    private double x, y, vy;
    private boolean active;

    /** Configure a bullet that has just come out of the pool. */
    public void fire(double x, double y, double vy) {
        this.x = x;
        this.y = y;
        this.vy = vy;
        this.active = true;
    }

    public void update(double dt) {
        y += vy * dt;
        if (y < -10) active = false;
    }

    public void render(GraphicsContext g) {
        g.setFill(Color.web("#B3261E"));
        g.fillRoundRect(x - 2, y - 8, 4, 12, 3, 3);
    }

    /** EVERY field, every time. This is the method students most often get wrong. */
    @Override
    public void reset() {
        x = 0;
        y = 0;
        vy = 0;
        active = false;
    }

    public void kill()          { active = false; }
    public boolean isActive()   { return active; }
    public double x()           { return x; }
    public double y()           { return y; }
}
