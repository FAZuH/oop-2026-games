package id.ac.ui.oop.s03towerdefense;

import id.ac.ui.oop.s03towerdefense.Events.GameEvent;
import id.ac.ui.oop.s03towerdefense.Events.Kind;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * SESSION 3 — the receiver that Commands act on, and the publisher of events.
 *
 * Notice what is NOT here: no HUD, no battle log, no sound. The world announces
 * what happened on the bus and stays ignorant of who reacts.
 */
public class World {

    /** The fixed route. No pathfinding — just points. */
    public static final double[][] PATH = {
            {-20, 430}, {120, 430}, {120, 250}, {330, 250},
            {330, 400}, {560, 400}, {560, 150}, {920, 150}
    };

    private final Events.EventBus bus;
    private final List<Tower> towers = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();

    private int gold = 220;      // enough to build, sell, undo and still experiment
    private int lives = 10;
    private int killed;
    private double spawnCooldown = 0.4;
    private int spawned;

    public World(Events.EventBus bus) {
        this.bus = bus;
    }

    // ---------------------------------------------------- command receivers

    /** Called by PlaceTowerCommand. Returns null when it is not allowed. */
    public Tower tryPlaceTower(double x, double y) {
        if (gold < Tower.COST) return null;
        if (tooCloseToPath(x, y)) return null;
        for (Tower t : towers) if (t.contains(x, y)) return null;

        Tower tower = new Tower(x, y);
        towers.add(tower);
        spend(Tower.COST);
        bus.publish(new GameEvent(Kind.TOWER_PLACED, "tower built", Tower.COST));
        return tower;
    }

    /** Called by SellTowerCommand. */
    public boolean trySellTower(Tower tower) {
        if (!towers.remove(tower)) return false;
        earn(Tower.REFUND);
        bus.publish(new GameEvent(Kind.TOWER_SOLD, "tower sold", Tower.REFUND));
        return true;
    }

    /** Undo of a placement: remove it and give the money back in full. */
    public void removeTowerRefunding(Tower tower) {
        if (towers.remove(tower)) earn(Tower.COST);
    }

    /** Undo of a sale: put it back and take the refund away again. */
    public void restoreTower(Tower tower) {
        towers.add(tower);
        spend(Tower.REFUND);
    }

    public Tower towerAt(double x, double y) {
        for (Tower t : towers) if (t.contains(x, y)) return t;
        return null;
    }

    // ------------------------------------------------------------ simulation

    public void update(double dt) {
        spawnCooldown -= dt;
        if (spawnCooldown <= 0 && spawned < 40) {
            spawnCooldown = 1.5;
            spawned++;
            enemies.add(new Enemy(PATH, 62 + spawned * 1.5, 3 + spawned / 6));
        }

        for (Tower tower : towers) tower.update(dt, enemies);
        for (Enemy enemy : enemies) enemy.update(dt);

        for (Iterator<Enemy> it = enemies.iterator(); it.hasNext(); ) {
            Enemy enemy = it.next();
            if (!enemy.isAlive()) {
                it.remove();
                killed++;
                earn(12);
                bus.publish(new GameEvent(Kind.ENEMY_KILLED, "enemy destroyed", 12));
            } else if (enemy.reachedEnd()) {
                it.remove();
                lives--;
                bus.publish(new GameEvent(Kind.ENEMY_LEAKED, "enemy reached the library", 1));
            }
        }
    }

    private boolean tooCloseToPath(double x, double y) {
        for (int i = 0; i < PATH.length - 1; i++) {
            if (distanceToSegment(x, y, PATH[i][0], PATH[i][1], PATH[i + 1][0], PATH[i + 1][1]) < 34) {
                return true;
            }
        }
        return false;
    }

    private static double distanceToSegment(double px, double py,
                                            double ax, double ay, double bx, double by) {
        double dx = bx - ax, dy = by - ay;
        double lengthSquared = dx * dx + dy * dy;
        double t = lengthSquared == 0 ? 0 : ((px - ax) * dx + (py - ay) * dy) / lengthSquared;
        t = Math.max(0, Math.min(1, t));
        return Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
    }

    private void spend(int amount) {
        gold -= amount;
        bus.publish(new GameEvent(Kind.GOLD_CHANGED, "spent", -amount));
    }

    private void earn(int amount) {
        gold += amount;
        bus.publish(new GameEvent(Kind.GOLD_CHANGED, "earned", amount));
    }

    public List<Tower> towers()  { return towers; }
    public List<Enemy> enemies() { return enemies; }
    public int gold()            { return gold; }
    public int lives()           { return lives; }
    public int killed()          { return killed; }
}
