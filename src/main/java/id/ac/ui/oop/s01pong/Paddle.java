package id.ac.ui.oop.s01pong;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * SESSION 1 — encapsulation.
 *
 * Every field is private. The outside world moves a paddle by asking it to
 * ({@link #moveTowards}), never by writing its coordinates. That is what lets
 * the paddle keep itself inside the court without anybody else remembering to.
 */
public class Paddle {

    public static final double WIDTH = 14;
    public static final double HEIGHT = 86;

    private final double x;
    private double y;
    private final double speed;      // pixels per SECOND, never per frame
    private final Color color;

    public Paddle(double x, double y, double speed, Color color) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.color = color;
    }

    /**
     * Moves toward a target y, but never further than this frame's time allows.
     * `dt` is seconds elapsed — multiplying by it is what makes the paddle move
     * at the same real-world speed on a fast machine and a slow one.
     */
    public void moveTowards(double targetY, double dt, double courtHeight) {
        double centre = y + HEIGHT / 2;
        double step = speed * dt;
        if (Math.abs(targetY - centre) > step) {
            y += (targetY > centre) ? step : -step;
        } else {
            y = targetY - HEIGHT / 2;
        }
        clampTo(courtHeight);
    }

    /** The paddle is responsible for staying on screen — nobody else is. */
    private void clampTo(double courtHeight) {
        if (y < 0) y = 0;
        if (y + HEIGHT > courtHeight) y = courtHeight - HEIGHT;
    }

    public boolean touches(Ball ball) {
        return ball.x() + ball.radius() > x
                && ball.x() - ball.radius() < x + WIDTH
                && ball.y() + ball.radius() > y
                && ball.y() - ball.radius() < y + HEIGHT;
    }

    /** Where the ball hit, as -1 (top edge) .. +1 (bottom edge). Drives the bounce angle. */
    public double hitOffset(Ball ball) {
        double centre = y + HEIGHT / 2;
        return Math.max(-1, Math.min(1, (ball.y() - centre) / (HEIGHT / 2)));
    }

    public void render(GraphicsContext g) {
        g.setFill(color);
        g.fillRoundRect(x, y, WIDTH, HEIGHT, 6, 6);
    }

    public double x() { return x; }
    public double y() { return y; }
}
