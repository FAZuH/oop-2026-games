package id.ac.ui.oop.s05breakout;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** SESSION 5 — a brick. Deliberately dull: the lesson is in how levels are BUILT. */
public class Brick {

    public static final double WIDTH = 62, HEIGHT = 22;

    private final double x, y;
    private int hits;
    private final Color color;

    public Brick(double x, double y, int hits, Color color) {
        this.x = x; this.y = y; this.hits = hits; this.color = color;
    }

    public boolean isAlive() { return hits > 0; }
    public void hit()        { hits--; }

    public boolean intersects(double bx, double by, double r) {
        return bx + r > x && bx - r < x + WIDTH && by + r > y && by - r < y + HEIGHT;
    }

    public void render(GraphicsContext g) {
        if (!isAlive()) return;
        g.setFill(hits > 1 ? color.darker() : color);
        g.fillRoundRect(x, y, WIDTH, HEIGHT, 4, 4);
    }

    public double centerY() { return y + HEIGHT / 2; }
    public double centerX() { return x + WIDTH / 2; }
}
