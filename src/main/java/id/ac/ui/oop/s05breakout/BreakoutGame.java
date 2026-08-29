package id.ac.ui.oop.s05breakout;

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

/**
 * SESSION 5 — Breakout.  Patterns: Template Method, and Facade.
 *
 * WHY THIS GAME FOR THIS SESSION
 *   Breakout has levels, and levels are built by a fixed recipe with variable
 *   ingredients — which is exactly Template Method. Press N to switch between
 *   two levels built by the SAME algorithm: the layout, the ball speed and the
 *   optional twist differ, the build order never does.
 *
 *   Notice how short this class is. Everything the game actually does lives
 *   behind GameEngine, the Facade. This class only draws and forwards keys.
 *
 * CONTROLS
 *   Left / Right or A / D : move paddle    N : next level    Space : mouse-free autoplay
 */
public class BreakoutGame extends Application {

    private static final double W = 900, H = 520;

    private final Canvas canvas = new Canvas(W, H);
    private GameEngine engine;                 // the Facade — the ONLY thing we talk to

    private boolean left, right, autoplay;
    private long lastNanos;
    private double elapsed, fps;

    @Override
    public void start(Stage stage) {
        engine = new GameEngine(W, H);
        engine.startLevel(0);                  // one call hides the whole build

        Scene scene = new Scene(new StackPane(canvas), W, H);
        scene.setOnKeyPressed(e -> {
            KeyCode c = e.getCode();
            if (c == KeyCode.LEFT  || c == KeyCode.A) left = true;
            if (c == KeyCode.RIGHT || c == KeyCode.D) right = true;
            if (c == KeyCode.N) engine.nextLevel();
            if (c == KeyCode.SPACE) autoplay = !autoplay;
        });
        scene.setOnKeyReleased(e -> {
            KeyCode c = e.getCode();
            if (c == KeyCode.LEFT  || c == KeyCode.A) left = false;
            if (c == KeyCode.RIGHT || c == KeyCode.D) right = false;
        });

        stage.setTitle("Session 5 — Breakout: Template Method & Facade");
        stage.setScene(scene);
        stage.show();
        canvas.requestFocus();

        if (Screenshot.requested()) { engine.startLevel(1); autoplay = true; }

        new AnimationTimer() {
            @Override public void handle(long now) {
                if (lastNanos == 0) { lastNanos = now; return; }
                double dt = Math.min((now - lastNanos) / 1_000_000_000.0, 0.25);
                lastNanos = now;
                elapsed += dt;
                fps = fps * 0.9 + (1 / dt) * 0.1;

                double target = engine.paddleX();
                if (autoplay)   target = engine.ballX();
                else if (left)  target = engine.paddleX() - 300 * dt * 60;
                else if (right) target = engine.paddleX() + 300 * dt * 60;
                engine.movePaddle(target, dt);
                engine.update(dt);
                render();

                if (Screenshot.requested() && elapsed >= Screenshot.after()) {
                    stop();
                    Screenshot.captureAndExit(canvas, "S05 Breakout");
                }
            }
        }.start();
    }

    private void render() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.web("#EAF5FA"));
        g.fillRect(0, 0, W, H);

        for (Brick brick : engine.bricks()) brick.render(g);

        g.setFill(Color.web("#0B5FA5"));
        g.fillRoundRect(engine.paddleX() - 56, H - 34, 112, 14, 7, 7);
        g.setFill(Color.web("#10233A"));
        g.fillOval(engine.ballX() - 8, engine.ballY() - 8, 16, 16);

        g.setFont(Font.font("Consolas", 13));
        g.setFill(Color.web("#10233A"));
        g.fillText(String.format("level %d  \"%s\"    score %d    lives %d    fps %.0f",
                engine.levelNumber(), engine.levelName(), engine.score(), engine.lives(), fps), 16, 24);
        g.setFill(Color.web("#4A6076"));
        g.fillText("N: next level (same template, different steps)   Space: autoplay   A/D: paddle",
                16, H - 12);
    }

    public static void main(String[] args) { launch(args); }
}
