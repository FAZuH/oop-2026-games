package id.ac.ui.oop.s01pong;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * SESSION 1 — the update/render split, and why delta time matters.
 *
 * {@link #update} WRITES state. {@link #render} only READS it. Keeping those
 * two jobs apart is the discipline the whole course rests on: if drawing code
 * also moved things, the game would speed up on a machine that draws faster.
 */
public class Ball {

    private static final Random RNG = new Random(7);   // fixed seed => demos repeat

    private double x, y;
    private double vx, vy;                              // pixels per SECOND
    private final double radius = 9;
    private double speed;

    public Ball(double x, double y, double speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        launch(RNG.nextBoolean() ? 1 : -1);
    }

    private void launch(int direction) {
        double angle = (RNG.nextDouble() - 0.5) * 0.8;  // shallow, playable angle
        vx = Math.cos(angle) * speed * direction;
        vy = Math.sin(angle) * speed;
    }

    /** Advance the simulation by `dt` seconds. Nothing here draws. */
    public void update(double dt, double courtHeight) {
        x += vx * dt;
        y += vy * dt;

        if (y - radius < 0) { y = radius; vy = -vy; }
        if (y + radius > courtHeight) { y = courtHeight - radius; vy = -vy; }
    }

    /** Bounce off a paddle, steering by where it was struck. */
    public void bounceOff(Paddle paddle, double offset) {
        speed = Math.min(speed * 1.04, 900);            // rallies get faster
        double angle = offset * 0.9;
        int direction = (vx > 0) ? -1 : 1;
        vx = Math.cos(angle) * speed * direction;
        vy = Math.sin(angle) * speed;
        // push clear so we cannot collide twice with the same paddle
        x = (direction > 0) ? paddle.x() + Paddle.WIDTH + radius + 1
                            : paddle.x() - radius - 1;
    }

    public void resetTo(double cx, double cy, int direction, double baseSpeed) {
        x = cx; y = cy; speed = baseSpeed;
        launch(direction);
    }

    /** Draw only. No state changes allowed in here. */
    public void render(GraphicsContext g) {
        g.setFill(Color.web("#10233A"));
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    public double x() { return x; }
    public double y() { return y; }
    public double vx() { return vx; }
    public double radius() { return radius; }
    public double speed() { return speed; }
}
