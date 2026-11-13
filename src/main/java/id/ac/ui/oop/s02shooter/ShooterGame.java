package id.ac.ui.oop.s02shooter;

import id.ac.ui.oop.devtools.Screenshot;
import id.ac.ui.oop.s02shooter.Enemies.Enemy;
import id.ac.ui.oop.s02shooter.Enemies.EnemyFactory;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * SESSION 2 — Shooter.  Patterns: Factory Method, and Object Pool.
 *
 * WHY THIS GAME FOR THIS SESSION
 *   A shooter is the clearest possible motivation for pooling: it fires far
 *   more bullets than anything else students build, so the "created vs reused"
 *   counter on the HUD tells the story by itself. It also needs several kinds
 *   of enemy, which is what Factory Method is for.
 *
 * CONTROLS
 *   Left / Right or A / D : move        Space : hold to fire
 *   P : toggle pooling OFF/ON — watch the "created" counter explode with it off
 */
public class ShooterGame extends Application {

    private static final double W = 900, H = 520;

    private final Canvas canvas = new Canvas(W, H);
    private final Random rng = new Random(11);

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private Pooling.ObjectPool<Bullet> bulletPool;

    // The spawner knows only this interface — never a concrete enemy class.
    private final EnemyFactory[] factories = {
            new Enemies.GruntFactory(), new Enemies.ZigzagFactory(), new Enemies.TankFactory(), new Enemies.BeanFactory()
    };

    private double shipX = W / 2;
    private boolean left, right, firing;
    private double fireCooldown, spawnCooldown;
    private int killed, escaped;
    private int allocationsWithoutPool;
    private boolean poolingOn = true;

    private long lastNanos;
    private double elapsed, fps;

    @Override
    public void start(Stage stage) {
        bulletPool = new Pooling.ObjectPool<>(Bullet::new, 40);

        Scene scene = new Scene(new StackPane(canvas), W, H);
        scene.setOnKeyPressed(e -> {
            KeyCode c = e.getCode();
            if (c == KeyCode.LEFT  || c == KeyCode.A) left = true;
            if (c == KeyCode.RIGHT || c == KeyCode.D) right = true;
            if (c == KeyCode.SPACE) firing = true;
            if (c == KeyCode.P) poolingOn = !poolingOn;
        });
        scene.setOnKeyReleased(e -> {
            KeyCode c = e.getCode();
            if (c == KeyCode.LEFT  || c == KeyCode.A) left = false;
            if (c == KeyCode.RIGHT || c == KeyCode.D) right = false;
            if (c == KeyCode.SPACE) firing = false;
        });

        stage.setTitle("Session 2 — Shooter: Factory Method & Object Pool");
        stage.setScene(scene);
        stage.show();
        canvas.requestFocus();

        new AnimationTimer() {
            @Override public void handle(long now) {
                if (lastNanos == 0) { lastNanos = now; return; }
                double dt = Math.min((now - lastNanos) / 1_000_000_000.0, 0.25);
                lastNanos = now;
                elapsed += dt;
                fps = fps * 0.9 + (1 / dt) * 0.1;

                update(dt);
                render();

                if (Screenshot.requested() && elapsed >= Screenshot.after()) {
                    stop();
                    Screenshot.captureAndExit(canvas, "S02 Shooter");
                }
            }
        }.start();
    }

    private void update(double dt) {
        if (left)  shipX -= 430 * dt;
        if (right) shipX += 430 * dt;
        shipX = Math.max(24, Math.min(W - 24, shipX));

        // ---- spawn through a factory chosen at random -------------------
        spawnCooldown -= dt;
        if (spawnCooldown <= 0) {
            spawnCooldown = 0.55;
            EnemyFactory factory = factories[rng.nextInt(factories.length)];
            enemies.add(factory.create(40 + rng.nextDouble() * (W - 80)));
        }

        // ---- fire (the demo auto-fires so the pool is exercised) ---------
        fireCooldown -= dt;
        boolean wantShot = firing || Screenshot.requested() || elapsed < 60;
        if (wantShot && fireCooldown <= 0) {
            fireCooldown = 0.07;
            Bullet b;
            if (poolingOn) {
                b = bulletPool.obtain();          // recycled
            } else {
                b = new Bullet();                  // a fresh allocation, every shot
                allocationsWithoutPool++;
            }
            b.fire(shipX, H - 60, -560);
            bullets.add(b);
        }

        for (Enemy e : enemies) e.update(dt);

        // ---- bullets: update, then return dead ones to the pool ----------
        for (Iterator<Bullet> it = bullets.iterator(); it.hasNext(); ) {
            Bullet b = it.next();
            b.update(dt);
            if (!b.isActive()) {
                it.remove();
                if (poolingOn) bulletPool.free(b);   // forget this and the pool starves
            }
        }

        // ---- collisions --------------------------------------------------
        for (Iterator<Enemy> ei = enemies.iterator(); ei.hasNext(); ) {
            Enemy e = ei.next();
            for (Bullet b : bullets) {
                if (b.isActive() && e.hitBy(b)) {
                    e.damage();
                    b.kill();
                }
            }
            if (e.isDead())              { ei.remove(); killed++; }
            else if (e.isOffScreen(H))   { ei.remove(); escaped++; }
        }
    }

    private void render() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.web("#EAF5FA"));
        g.fillRect(0, 0, W, H);

        for (Enemy e : enemies) e.render(g);
        for (Bullet b : bullets) if (b.isActive()) b.render(g);

        g.setFill(Color.web("#10233A"));
        g.fillPolygon(new double[]{shipX, shipX - 17, shipX + 17},
                      new double[]{H - 62, H - 30, H - 30}, 3);

        g.setFont(Font.font("Consolas", 13));
        g.setFill(Color.web("#10233A"));
        g.fillText(String.format("fps %.0f   enemies %d   killed %d   escaped %d",
                fps, enemies.size(), killed, escaped), 16, 24);

        // The lesson, on screen: allocations avoided.
        g.setFill(poolingOn ? Color.web("#0B5FA5") : Color.web("#B3261E"));
        if (poolingOn) {
            g.fillText(String.format("POOL ON  (P to disable)   objects created %d   reused %d   live %d   idle %d",
                    bulletPool.created(), bulletPool.reused(), bulletPool.live(), bulletPool.idle()), 16, 44);
            g.fillText("Only " + bulletPool.created()
                    + " Bullet objects have ever been built, no matter how long you fire.", 16, 64);
        } else {
            g.fillText(String.format("POOL OFF (P to enable)    `new Bullet()` calls so far: %d",
                    allocationsWithoutPool), 16, 44);
            g.fillText("Every shot allocates. This is what makes the garbage collector stutter.", 16, 64);
        }
    }

    public static void main(String[] args) { launch(args); }
}
