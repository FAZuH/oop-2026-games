package id.ac.ui.oop.s06snake;

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

import java.nio.file.Path;
import java.util.List;

/**
 * SESSION 6 — Snake.  Topics: file I/O, exception design, and unit testing.
 *
 * WHY THIS GAME FOR THIS SESSION
 *   Snake's rules are pure logic on a grid, so they can be tested without a
 *   window — which is the whole point of this session. SnakeLogic has no
 *   JavaFX import in it, SnakeLogicTest exercises it directly, and this class
 *   does nothing but draw and forward keys.
 *
 *   High scores give us the second topic: a file that may be missing (normal)
 *   or corrupt (someone edited it), handled without ever showing the player a
 *   stack trace.
 *
 * CONTROLS
 *   Arrows / WASD : turn      Space : new game      S : save the current score
 */
public class SnakeGame extends Application {

    private static final int CELL = 22, COLUMNS = 34, ROWS = 20;
    private static final double W = CELL * COLUMNS, H = CELL * ROWS + 78;

    private final Canvas canvas = new Canvas(W, H);
    private final HighScoreRepository scores =
            new HighScoreRepository(Path.of("build", "scores", "snake-highscores.csv"));

    private SnakeLogic logic;
    private List<HighScoreRepository.Entry> table;
    private String status = "";
    private double tickAccumulator;
    private long lastNanos;
    private double elapsed;

    @Override
    public void start(Stage stage) {
        logic = new SnakeLogic(COLUMNS, ROWS, 42);
        table = scores.load();                       // never throws
        status = scores.warnings().isEmpty()
                ? "loaded " + table.size() + " high scores"
                : String.join("; ", scores.warnings());

        Scene scene = new Scene(new StackPane(canvas), W, H);
        scene.setOnKeyPressed(e -> {
            KeyCode c = e.getCode();
            if (c == KeyCode.UP    || c == KeyCode.W) logic.turn(SnakeLogic.Direction.UP);
            if (c == KeyCode.DOWN  || c == KeyCode.S && e.isShiftDown()) logic.turn(SnakeLogic.Direction.DOWN);
            if (c == KeyCode.LEFT  || c == KeyCode.A) logic.turn(SnakeLogic.Direction.LEFT);
            if (c == KeyCode.RIGHT || c == KeyCode.D) logic.turn(SnakeLogic.Direction.RIGHT);
            if (c == KeyCode.SPACE) { logic = new SnakeLogic(COLUMNS, ROWS, System.nanoTime()); status = "new game"; }
            if (c == KeyCode.S && !e.isShiftDown()) saveScore();
        });

        stage.setTitle("Session 6 — Snake: Persistence, Exceptions & Testing");
        stage.setScene(scene);
        stage.show();
        canvas.requestFocus();

        new AnimationTimer() {
            @Override public void handle(long now) {
                if (lastNanos == 0) { lastNanos = now; return; }
                double dt = Math.min((now - lastNanos) / 1_000_000_000.0, 0.25);
                lastNanos = now;
                elapsed += dt;

                tickAccumulator += dt;
                while (tickAccumulator >= 0.11) {      // fixed logic tick
                    tickAccumulator -= 0.11;
                    if (Screenshot.requested()) autoPilot();
                    logic.step();
                }
                render();

                if (Screenshot.requested() && elapsed >= Screenshot.after()) {
                    stop();
                    Screenshot.captureAndExit(canvas, "S06 Snake");
                }
            }
        }.start();
    }

    /** Only for the unattended screenshot: steer roughly toward the food. */
    private void autoPilot() {
        if (!logic.isAlive() || logic.food() == null) return;
        SnakeLogic.Cell head = logic.head(), food = logic.food();
        if (food.x() != head.x()) {
            logic.turn(food.x() > head.x() ? SnakeLogic.Direction.RIGHT : SnakeLogic.Direction.LEFT);
        } else if (food.y() != head.y()) {
            logic.turn(food.y() > head.y() ? SnakeLogic.Direction.DOWN : SnakeLogic.Direction.UP);
        }
    }

    /** The caller decides what a failed save means — hence a checked exception. */
    private void saveScore() {
        try {
            table = scores.record("player", logic.score());
            status = "saved — top score is now " + (table.isEmpty() ? 0 : table.get(0).score());
        } catch (HighScoreRepository.ScoreStoreException e) {
            status = "could not save: " + e.getMessage();   // no stack trace for the player
        }
    }

    private void render() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.web("#EAF5FA"));
        g.fillRect(0, 0, W, H);

        g.setStroke(Color.web("#CFDDE6"));
        g.setLineWidth(1);
        for (int x = 0; x <= COLUMNS; x++) g.strokeLine(x * CELL, 0, x * CELL, ROWS * CELL);
        for (int y = 0; y <= ROWS; y++) g.strokeLine(0, y * CELL, W, y * CELL);

        if (logic.food() != null) {
            g.setFill(Color.web("#B3261E"));
            g.fillOval(logic.food().x() * CELL + 4, logic.food().y() * CELL + 4, CELL - 8, CELL - 8);
        }
        boolean isHead = true;
        for (SnakeLogic.Cell cell : logic.body()) {
            g.setFill(isHead ? Color.web("#10233A") : Color.web("#0B5FA5"));
            g.fillRoundRect(cell.x() * CELL + 2, cell.y() * CELL + 2, CELL - 4, CELL - 4, 5, 5);
            isHead = false;
        }

        g.setFont(Font.font("Consolas", 13));
        g.setFill(Color.web("#10233A"));
        g.fillText("score " + logic.score() + (logic.isAlive() ? "" : "   —  DEAD, Space for a new game")
                + "    length " + logic.body().size(), 12, ROWS * CELL + 20);
        g.setFill(Color.web("#4A6076"));
        g.fillText("S: save score (may throw — handled)    file: build/scores/snake-highscores.csv",
                12, ROWS * CELL + 38);
        g.setFill(Color.web("#0B5FA5"));
        g.fillText(status, 12, ROWS * CELL + 56);

        int column = 0;
        for (HighScoreRepository.Entry entry : table) {
            g.setFill(Color.web("#4A6076"));
            g.fillText(entry.name() + " " + entry.score(), W - 240 + (column % 2) * 110,
                    ROWS * CELL + 20 + (column / 2) * 18);
            column++;
        }
    }

    public static void main(String[] args) { launch(args); }
}
