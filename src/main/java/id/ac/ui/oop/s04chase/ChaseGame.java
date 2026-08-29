package id.ac.ui.oop.s04chase;

import id.ac.ui.oop.devtools.Screenshot;
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
import java.util.List;

/**
 * SESSION 4 — Chase.  Patterns: Strategy, and State.
 *
 * WHY THIS GAME FOR THIS SESSION
 *   Hunter behaviour is the textbook home for Strategy, and here you can SEE it:
 *   press 1, 2 or 3 and every hunter changes how it moves, immediately, with no
 *   new class created. Meanwhile the game itself is obviously a state machine —
 *   Menu, Playing, Paused, Game Over — each with different rules for the same
 *   keypress. Space means "start" in one state and "restart" in another.
 *
 * CONTROLS
 *   Arrows / WASD : move     1 2 3 : swap every hunter's strategy
 *   Space : start / restart  P : pause      Esc : back to menu
 */
public class ChaseGame extends Application {

    private static final double W = 900, H = 520;

    // ---------------------------------------------------------------- State

    /**
     * SESSION 4 — the State pattern. Each state owns its own update, render and
     * input handling, AND decides which state comes next. The game object holds
     * one reference and delegates; there is no switch anywhere.
     */
    private interface GameState {
        void update(double dt);
        void render(GraphicsContext g);
        void onKey(KeyCode code);
        String label();
    }

    private final Canvas canvas = new Canvas(W, H);
    private GameState state;

    // world
    private double px = W / 2, py = H / 2, pvx, pvy;
    private final List<Hunter> hunters = new ArrayList<>();
    private ChaseStrategy strategy = new ChaseStrategy.Direct();
    private double survived;
    private double best;

    private boolean up, down, left, right;
    private long lastNanos;
    private double elapsed, fps;

    /** A hunter HAS-A strategy. Change the strategy, change the behaviour. */
    private static final class Hunter {
        double x, y;
        final double speed;
        final Color color;
        Hunter(double x, double y, double speed, Color color) {
            this.x = x; this.y = y; this.speed = speed; this.color = color;
        }
        void update(double dt, ChaseStrategy strategy,
                    double px, double py, double pvx, double pvy) {
            double[] aim = strategy.aimFor(x, y, px, py, pvx, pvy);
            double dx = aim[0] - x, dy = aim[1] - y;
            double distance = Math.hypot(dx, dy);
            if (distance > 1) {
                x += dx / distance * speed * dt;
                y += dy / distance * speed * dt;
            }
        }
        boolean caught(double px, double py) { return Math.hypot(px - x, py - y) < 22; }
    }

    @Override
    public void start(Stage stage) {
        state = new MenuState();

        Scene scene = new Scene(new StackPane(canvas), W, H);
        scene.setOnKeyPressed(e -> {
            KeyCode c = e.getCode();
            if (c == KeyCode.UP    || c == KeyCode.W) up = true;
            if (c == KeyCode.DOWN  || c == KeyCode.S) down = true;
            if (c == KeyCode.LEFT  || c == KeyCode.A) left = true;
            if (c == KeyCode.RIGHT || c == KeyCode.D) right = true;
            if (c == KeyCode.DIGIT1) strategy = new ChaseStrategy.Direct();
            if (c == KeyCode.DIGIT2) strategy = new ChaseStrategy.Predictive();
            if (c == KeyCode.DIGIT3) strategy = new ChaseStrategy.Flanking();
            state.onKey(c);                       // the STATE decides what it means
        });
        scene.setOnKeyReleased(e -> {
            KeyCode c = e.getCode();
            if (c == KeyCode.UP    || c == KeyCode.W) up = false;
            if (c == KeyCode.DOWN  || c == KeyCode.S) down = false;
            if (c == KeyCode.LEFT  || c == KeyCode.A) left = false;
            if (c == KeyCode.RIGHT || c == KeyCode.D) right = false;
        });

        stage.setTitle("Session 4 — Chase: Strategy & State");
        stage.setScene(scene);
        stage.show();
        canvas.requestFocus();

        if (Screenshot.requested()) { beginRound(); state = new PlayingState(); }

        new AnimationTimer() {
            @Override public void handle(long now) {
                if (lastNanos == 0) { lastNanos = now; return; }
                double dt = Math.min((now - lastNanos) / 1_000_000_000.0, 0.25);
                lastNanos = now;
                elapsed += dt;
                fps = fps * 0.9 + (1 / dt) * 0.1;

                state.update(dt);
                GraphicsContext g = canvas.getGraphicsContext2D();
                g.setFill(Color.web("#EAF5FA"));
                g.fillRect(0, 0, W, H);
                state.render(g);
                renderHud(g);

                if (Screenshot.requested() && elapsed >= Screenshot.after()) {
                    stop();
                    Screenshot.captureAndExit(canvas, "S04 Chase");
                }
            }
        }.start();
    }

    private void beginRound() {
        px = W / 2; py = H / 2; pvx = pvy = 0; survived = 0;
        hunters.clear();
        hunters.add(new Hunter(60, 60, 132, Color.web("#B3261E")));
        hunters.add(new Hunter(W - 60, 70, 118, Color.web("#7A4B9B")));
        hunters.add(new Hunter(80, H - 70, 124, Color.web("#0E7C6B")));
    }

    private void renderHud(GraphicsContext g) {
        g.setFont(Font.font("Consolas", 13));
        g.setFill(Color.web("#10233A"));
        g.fillText(String.format("state: %-10s  fps %.0f   survived %.1fs   best %.1fs",
                state.label(), fps, survived, best), 16, 24);
        g.setFill(Color.web("#0B5FA5"));
        g.fillText("strategy [1/2/3]: " + strategy.name(), 16, 44);
    }

    // ------------------------------------------------------- concrete states

    private final class MenuState implements GameState {
        @Override public void update(double dt) { }
        @Override public void render(GraphicsContext g) {
            g.setFill(Color.web("#10233A"));
            g.setFont(Font.font("Consolas", 30));
            g.fillText("CHASE", W / 2 - 55, H / 2 - 30);
            g.setFont(Font.font("Consolas", 15));
            g.fillText("Space to start.  Keys 1/2/3 swap hunter strategy mid-run.", W / 2 - 250, H / 2 + 10);
        }
        @Override public void onKey(KeyCode code) {
            if (code == KeyCode.SPACE) { beginRound(); state = new PlayingState(); }
        }
        @Override public String label() { return "Menu"; }
    }

    private final class PlayingState implements GameState {
        @Override public void update(double dt) {
            survived += dt;
            double speed = 250;
            pvx = (right ? speed : 0) - (left ? speed : 0);
            pvy = (down ? speed : 0) - (up ? speed : 0);
            px = Math.max(12, Math.min(W - 12, px + pvx * dt));
            py = Math.max(12, Math.min(H - 12, py + pvy * dt));

            for (Hunter h : hunters) {
                h.update(dt, strategy, px, py, pvx, pvy);
                if (h.caught(px, py)) {
                    best = Math.max(best, survived);
                    state = new GameOverState();
                }
            }
        }
        @Override public void render(GraphicsContext g) { drawWorld(g); }
        @Override public void onKey(KeyCode code) {
            if (code == KeyCode.P)   state = new PausedState();
            if (code == KeyCode.ESCAPE) state = new MenuState();
        }
        @Override public String label() { return "Playing"; }
    }

    private final class PausedState implements GameState {
        @Override public void update(double dt) { /* frozen: that IS the behaviour */ }
        @Override public void render(GraphicsContext g) {
            drawWorld(g);
            g.setFill(Color.web("#10233A", 0.75));
            g.setFont(Font.font("Consolas", 26));
            g.fillText("PAUSED — P to resume", W / 2 - 170, H / 2);
        }
        @Override public void onKey(KeyCode code) {
            if (code == KeyCode.P) state = new PlayingState();
            if (code == KeyCode.ESCAPE) state = new MenuState();
        }
        @Override public String label() { return "Paused"; }
    }

    private final class GameOverState implements GameState {
        @Override public void update(double dt) { }
        @Override public void render(GraphicsContext g) {
            drawWorld(g);
            g.setFill(Color.web("#B3261E"));
            g.setFont(Font.font("Consolas", 26));
            g.fillText("CAUGHT — Space to try again", W / 2 - 210, H / 2);
        }
        @Override public void onKey(KeyCode code) {
            // Space means something DIFFERENT here than in MenuState.
            if (code == KeyCode.SPACE) { beginRound(); state = new PlayingState(); }
            if (code == KeyCode.ESCAPE) state = new MenuState();
        }
        @Override public String label() { return "GameOver"; }
    }

    private void drawWorld(GraphicsContext g) {
        for (Hunter h : hunters) {
            g.setFill(h.color);
            g.fillOval(h.x - 13, h.y - 13, 26, 26);
        }
        g.setFill(Color.web("#0B5FA5"));
        g.fillRoundRect(px - 11, py - 11, 22, 22, 5, 5);
    }

    public static void main(String[] args) { launch(args); }
}
