package id.ac.ui.oop.s03towerdefense;

import id.ac.ui.oop.devtools.Screenshot;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * SESSION 3 — Tower Defense.  Patterns: Command, and Observer.
 *
 * WHY THIS GAME FOR THIS SESSION
 *   "Sell a tower, then change your mind" makes undo a feature players actually
 *   want, so Command stops being an academic exercise. And one enemy death has
 *   to move gold, the HUD and the battle log at once, which is exactly the
 *   one-to-many problem Observer solves.
 *
 * CONTROLS
 *   Left click  : place a tower (40g)     Right click : sell the tower under the cursor (+30g)
 *   Ctrl+Z      : undo the last action    L : add a battle-log listener at runtime
 */
public class TowerDefenseGame extends Application {

    private static final double W = 900, H = 520;

    private final Canvas canvas = new Canvas(W, H);
    private final Events.EventBus bus = new Events.EventBus();
    private final Commands.CommandHistory history = new Commands.CommandHistory();
    private final Deque<String> log = new ArrayDeque<>();
    private World world;

    private String lastAction = "—";
    private double mouseX = -100, mouseY = -100;
    private long lastNanos;
    private double elapsed, fps;

    @Override
    public void start(Stage stage) {
        world = new World(bus);

        // ---- three independent listeners, none of which the World knows ----
        bus.subscribe(event -> {                       // 1. the battle log
            log.push(event.detail() + (event.amount() != 0 ? " (" + event.amount() + ")" : ""));
            while (log.size() > 7) log.removeLast();
        });
        bus.subscribe(event -> {                       // 2. a "kill streak" watcher
            if (event.kind() == Events.Kind.ENEMY_LEAKED) lastAction = "! a leak — defend the path";
        });

        Scene scene = new Scene(new StackPane(canvas), W, H);
        canvas.setOnMouseMoved(e -> { mouseX = e.getX(); mouseY = e.getY(); });
        canvas.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                boolean ok = history.run(new Commands.PlaceTowerCommand(world, e.getX(), e.getY()));
                lastAction = ok ? "placed a tower" : "cannot build there (gold, path or overlap)";
            } else if (e.getButton() == MouseButton.SECONDARY) {
                Tower t = world.towerAt(e.getX(), e.getY());
                if (t != null) {
                    history.run(new Commands.SellTowerCommand(world, t));
                    lastAction = "sold a tower";
                }
            }
        });
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.Z && e.isControlDown()) {
                String undone = history.undoLast();
                lastAction = (undone == null) ? "nothing left to undo" : "undid: " + undone;
            } else if (e.getCode() == KeyCode.L) {
                // Adding behaviour at RUNTIME without touching World or Enemy.
                bus.subscribe(event -> log.push("  [extra listener] saw " + event.kind()));
                lastAction = "subscribed another listener (now " + bus.listenerCount() + ")";
            }
        });

        stage.setTitle("Session 3 — Tower Defense: Command & Observer");
        stage.setScene(scene);
        stage.show();
        canvas.requestFocus();

        if (Screenshot.requested()) seedDemoTowers();

        new AnimationTimer() {
            @Override public void handle(long now) {
                if (lastNanos == 0) { lastNanos = now; return; }
                double dt = Math.min((now - lastNanos) / 1_000_000_000.0, 0.25);
                lastNanos = now;
                elapsed += dt;
                fps = fps * 0.9 + (1 / dt) * 0.1;

                world.update(dt);
                render();

                if (Screenshot.requested() && elapsed >= Screenshot.after()) {
                    stop();
                    Screenshot.captureAndExit(canvas, "S03 TowerDefense");
                }
            }
        }.start();
    }

    /** For the unattended screenshot: place a few towers through the Command path. */
    private void seedDemoTowers() {
        history.run(new Commands.PlaceTowerCommand(world, 190, 330));
        history.run(new Commands.PlaceTowerCommand(world, 420, 320));
        history.run(new Commands.PlaceTowerCommand(world, 640, 250));
        lastAction = "3 towers placed via PlaceTowerCommand";
    }

    private void render() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.web("#EAF5FA"));
        g.fillRect(0, 0, W, H);

        // the fixed path
        g.setStroke(Color.web("#9FB4C4"));
        g.setLineWidth(30);
        strokePath(g);
        g.setStroke(Color.web("#CFDDE6"));
        g.setLineWidth(24);
        strokePath(g);

        Tower hovered = world.towerAt(mouseX, mouseY);
        for (Tower tower : world.towers()) tower.render(g, tower == hovered);
        for (Enemy enemy : world.enemies()) enemy.render(g);

        g.setFont(Font.font("Consolas", 13));
        g.setFill(Color.web("#10233A"));
        g.fillText(String.format("gold %d    lives %d    killed %d    fps %.0f",
                world.gold(), world.lives(), world.killed(), fps), 16, 24);
        g.fillText("undo stack: " + history.size() + "    listeners: " + bus.listenerCount(), 16, 44);
        g.setFill(Color.web("#0B5FA5"));
        g.fillText("last action -> " + lastAction, 16, 64);
        g.setFill(Color.web("#4A6076"));
        g.fillText("LMB place (40g)   RMB sell (+30g)   Ctrl+Z undo   L add listener", 16, H - 16);

        // the battle log — written purely by an Observer
        g.setFill(Color.web("#4A6076"));
        int row = 0;
        for (String line : log) {
            g.fillText(line, W - 300, 24 + (row++) * 16);
        }
    }

    private void strokePath(GraphicsContext g) {
        g.beginPath();
        g.moveTo(World.PATH[0][0], World.PATH[0][1]);
        for (int i = 1; i < World.PATH.length; i++) g.lineTo(World.PATH[i][0], World.PATH[i][1]);
        g.stroke();
    }

    public static void main(String[] args) { launch(args); }
}
