package id.ac.ui.oop.s01pong;

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
 * SESSION 1 — Pong.  Patterns: the Game Loop, and Singleton.
 *
 * WHY THIS GAME FOR THIS SESSION
 *   Pong is the smallest program in which delta time is *visibly* necessary.
 *   Press F to cap the frame rate: the naive ball (which moves a fixed amount
 *   per frame) crawls, while the dt-driven ball keeps its real speed. That one
 *   keypress is the whole argument for the update/render split.
 *
 * CONTROLS
 *   Up / Down or W / S : move your paddle       F : toggle a 15 fps cap
 *   N                  : toggle naive movement  R : reset the score
 */
public class PongGame extends Application {
    private AudioManager audio;

    private static final double W = 900, H = 520;
    private static final double BASE_SPEED = 330;

    private final Canvas canvas = new Canvas(W, H);
    private Paddle player, computer;
    private Ball ball;

    // input state
    private boolean up, down;

    // demo toggles — these exist to make the lesson visible
    private boolean frameCapped = false;   // F
    private boolean naiveMovement = false; // N
    private double capAccumulator = 0;

    // loop bookkeeping
    private long lastNanos = 0;
    private double fps = 0;
    private double elapsed = 0;

    @Override
    public void start(Stage stage) {
        player   = new Paddle(30,      H / 2 - Paddle.HEIGHT / 2, 520, Color.web("#0B5FA5"));
        computer = new Paddle(W - 44,  H / 2 - Paddle.HEIGHT / 2, 300, Color.web("#4A6076"));
        ball     = new Ball(W / 2, H / 2, BASE_SPEED);

        audio = AudioManager.getInstance();
        audio.init();

        Scene scene = new Scene(new StackPane(canvas), W, H);
        scene.setOnKeyPressed(e -> {
            KeyCode c = e.getCode();
            if (c == KeyCode.UP   || c == KeyCode.W) up = true;
            if (c == KeyCode.DOWN || c == KeyCode.S) down = true;
            if (c == KeyCode.F) frameCapped = !frameCapped;
            if (c == KeyCode.N) naiveMovement = !naiveMovement;
            if (c == KeyCode.R) ScoreManager.getInstance().reset();
        });
        scene.setOnKeyReleased(e -> {
            KeyCode c = e.getCode();
            if (c == KeyCode.UP   || c == KeyCode.W) up = false;
            if (c == KeyCode.DOWN || c == KeyCode.S) down = false;
        });

        stage.setTitle("Session 1 — Pong: Game Loop & Singleton");
        stage.setScene(scene);
        stage.show();
        canvas.requestFocus();

        // ---------------- THE GAME LOOP ----------------
        // JavaFX calls handle() once per display frame and hands us a
        // timestamp in nanoseconds. Deriving dt from consecutive timestamps
        // is the whole trick; everything downstream is multiplied by it.
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastNanos == 0) { lastNanos = now; return; }
                double dt = (now - lastNanos) / 1_000_000_000.0;
                lastNanos = now;
                if (dt > 0.25) dt = 0.25;             // ignore a debugger pause
                elapsed += dt;
                fps = fps * 0.9 + (1.0 / dt) * 0.1;   // smoothed, for the HUD

                if (frameCapped) {                    // simulate a slow machine
                    capAccumulator += dt;
                    if (capAccumulator < 1.0 / 15) { render(); return; }
                    dt = capAccumulator;
                    capAccumulator = 0;
                }

                update(dt);
                render();

                if (Screenshot.requested() && elapsed >= Screenshot.after()) {
                    stop();
                    Screenshot.captureAndExit(canvas, "S01 Pong");
                }
            }
        }.start();
    }

    /** Phase 1 of a frame: change the world. Never draws. */
    private void update(double dt) {
        double target = player.y() + Paddle.HEIGHT / 2;
        if (up)   target -= 400 * dt;
        if (down) target += 400 * dt;
        player.moveTowards(target, dt, H);

        // A deliberately dumb opponent: it lags, so the game is winnable.
        computer.moveTowards(ball.y(), dt, H);

        // The teaching switch. Naive movement ignores dt and moves a fixed
        // amount per FRAME, so the ball's real speed follows the frame rate.
        ball.update(naiveMovement ? (1.0 / 60) : dt, H);

        if (ball.vx() < 0 && player.touches(ball)) {
            ball.bounceOff(player, player.hitOffset(ball));
            audio.playSound("hit");
        } else if (ball.vx() > 0 && computer.touches(ball)) {
            ball.bounceOff(computer, computer.hitOffset(ball));
            audio.playSound("hit");
        }

        // Scoring goes through the Singleton — no ScoreManager was passed in.
        if (ball.x() < -20) {
            ScoreManager.getInstance().awardComputer();
            ball.resetTo(W / 2, H / 2, 1, BASE_SPEED);
            audio.playSound("score");
        } else if (ball.x() > W + 20) {
            ScoreManager.getInstance().awardPlayer();
            ball.resetTo(W / 2, H / 2, -1, BASE_SPEED);
            audio.playSound("score");
        }
    }

    /** Phase 2 of a frame: draw the world. Never changes it. */
    private void render() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        ScoreManager score = ScoreManager.getInstance();

        g.setFill(Color.web("#EAF5FA"));
        g.fillRect(0, 0, W, H);

        g.setStroke(Color.web("#CFDDE6"));
        g.setLineWidth(3);
        g.setLineDashes(12, 12);
        g.strokeLine(W / 2, 12, W / 2, H - 12);
        g.setLineDashes(null);

        player.render(g);
        computer.render(g);
        ball.render(g);

        g.setFill(Color.web("#0B5FA5"));
        g.setFont(Font.font("Consolas", 44));
        g.fillText(String.valueOf(score.playerScore()), W / 2 - 70, 58);
        g.setFill(Color.web("#4A6076"));
        g.fillText(String.valueOf(score.computerScore()), W / 2 + 40, 58);

        g.setFont(Font.font("Consolas", 13));
        g.setFill(Color.web("#10233A"));
        g.fillText(String.format("fps %.0f    ball %.0f px/s    rallies %d",
                fps, ball.speed(), score.rallies()), 16, H - 40);
        g.setFill(naiveMovement ? Color.web("#B3261E") : Color.web("#4A6076"));
        g.fillText(naiveMovement
                        ? "N: NAIVE movement (fixed step per frame) — press F and watch it break"
                        : "N: dt-driven movement (correct)          F: frame cap "
                          + (frameCapped ? "ON (15 fps)" : "off"),
                16, H - 20);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
