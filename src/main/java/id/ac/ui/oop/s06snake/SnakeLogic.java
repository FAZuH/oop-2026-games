package id.ac.ui.oop.s06snake;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * SESSION 6 — game logic with NO JavaFX in it, so it can be unit tested.
 *
 * This is the session's quiet lesson: the reason we can write tests for this
 * game is a design decision taken much earlier — keeping the rules separate
 * from the drawing. You cannot bolt testability on afterwards.
 *
 * Everything here is deterministic given a seed, which is what makes the tests
 * in SnakeLogicTest repeatable.
 */
public class SnakeLogic {

    public enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
        final int dx, dy;
        Direction(int dx, int dy) { this.dx = dx; this.dy = dy; }
        /** You may not reverse straight into your own neck. */
        public boolean isOpposite(Direction other) {
            return dx + other.dx == 0 && dy + other.dy == 0;
        }
    }

    public record Cell(int x, int y) { }

    private final int columns, rows;
    private final Deque<Cell> body = new ArrayDeque<>();
    private Direction direction = Direction.RIGHT;
    private Direction queued = Direction.RIGHT;
    private Cell food;
    private int score;
    private boolean alive = true;
    private final Random rng;

    public SnakeLogic(int columns, int rows, long seed) {
        this.columns = columns;
        this.rows = rows;
        this.rng = new Random(seed);
        body.add(new Cell(columns / 2, rows / 2));
        body.add(new Cell(columns / 2 - 1, rows / 2));
        placeFood();
    }

    /** Queued, not applied immediately: two turns in one tick would be cheating. */
    public void turn(Direction next) {
        if (!next.isOpposite(direction)) queued = next;
    }

    /** Advance one tick. Returns false once the snake has died. */
    public boolean step() {
        if (!alive) return false;
        direction = queued;

        Cell head = body.peekFirst();
        Cell next = new Cell(head.x() + direction.dx, head.y() + direction.dy);

        if (next.x() < 0 || next.y() < 0 || next.x() >= columns || next.y() >= rows) {
            alive = false;
            return false;
        }
        // Walking onto the tail tip is legal, because it moves away this tick.
        Cell tail = body.peekLast();
        for (Cell cell : body) {
            if (cell.equals(next) && !(cell.equals(tail) && !next.equals(food))) {
                alive = false;
                return false;
            }
        }

        body.addFirst(next);
        if (next.equals(food)) {
            score += 10;
            placeFood();
        } else {
            body.removeLast();
        }
        return true;
    }

    private void placeFood() {
        for (int attempt = 0; attempt < 500; attempt++) {
            Cell candidate = new Cell(rng.nextInt(columns), rng.nextInt(rows));
            if (!body.contains(candidate)) { food = candidate; return; }
        }
        food = null;   // board full: the player has won
    }

    public List<Cell> body()   { return List.copyOf(body); }
    public Cell head()         { return body.peekFirst(); }
    public Cell food()         { return food; }
    public int score()         { return score; }
    public boolean isAlive()   { return alive; }
    public int columns()       { return columns; }
    public int rows()          { return rows; }
}
