package id.ac.ui.oop.s02shooter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 •  SESSION 2 — inheritance, polymorphism, and the Factory Method pattern.
 *
 •  Three enemies share a base class but move differently. The game loop never
 •  asks "which kind is this?" — it just calls update() and lets each type answer
 •  for itself. That is polymorphism doing the work an if-else chain would do.
 *
 •  Each enemy also gets its own factory. Adding a fourth enemy type means adding
 •  two new classes and touching NOTHING else — the Open/Closed Principle.
 */
public final class Enemies {

    private Enemies() { }

    // ---------------------------------------------------------------- product

    public abstract static class Enemy {
        protected double x, y;
        protected double speed;
        protected int health;
        protected final double radius;
        protected final Color color;
        private final String name;

        protected Enemy(double x, double y, double speed, int health, double radius,
                        Color color, String name) {
            this.x = x; this.y = y; this.speed = speed; this.health = health;
            this.radius = radius; this.color = color; this.name = name;
        }

        /** Each subclass supplies its own movement. Same signature, different behaviour. */
        public abstract void update(double dt);

        public void render(GraphicsContext g) {
            g.setFill(color);
            g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            g.setFill(Color.WHITE);
            g.fillText(String.valueOf(health), x - 3, y + 4);
        }

        public boolean hitBy(Bullet b) {
            double dx = b.x() - x, dy = b.y() - y;
            return dx * dx + dy * dy < (radius + 4) * (radius + 4);
        }

        public void damage()        { health--; }
        public boolean isDead()     { return health <= 0; }
        public boolean isOffScreen(double h) { return y - radius > h; }
        public double x()           { return x; }
        public double y()           { return y; }
        public String name()        { return name; }
    }

    /** Straight down, weak. */
    public static final class Grunt extends Enemy {
        public Grunt(double x) { super(x, -20, 95, 1, 13, Color.web("#0B5FA5"), "Grunt"); }
        @Override public void update(double dt) { y += speed * dt; }
    }

    /** Weaves as it descends — same interface, visibly different motion. */
    public static final class Zigzag extends Enemy {
        private double phase;
        public Zigzag(double x) { super(x, -20, 78, 2, 13, Color.web("#0E7C6B"), "Zigzag"); }
        @Override public void update(double dt) {
            phase += dt * 3.4;
            y += speed * dt;
            x += Math.cos(phase) * 92 * dt;
        }
    }

    /** Slow and tough. */
    public static final class Tank extends Enemy {
        public Tank(double x) { super(x, -26, 46, 6, 19, Color.web("#7A4B9B"), "Tank"); }
        @Override public void update(double dt) { y += speed * dt; }
    }

    public static final class Bean extends Enemy {
        public Bean(double x) { super(x, -26, 70, 1, 10, Color.web("#03befc"), "Bean"); }
        @Override public void update(double dt) {
            y += speed * dt;
            x += speed * dt;
        }
    }


    // ---------------------------------------------------------------- creator

    /**
     •  The Factory Method. The spawner holds one of these and calls create();
     •  it never mentions Grunt, Zigzag or Tank by name.
     */
    public interface EnemyFactory {
        Enemy create(double x);
        String label();
    }

    public static final class GruntFactory implements EnemyFactory {
        @Override public Enemy create(double x) { return new Grunt(x); }
        @Override public String label() { return "GruntFactory"; }
    }

    public static final class ZigzagFactory implements EnemyFactory {
        @Override public Enemy create(double x) { return new Zigzag(x); }
        @Override public String label() { return "ZigzagFactory"; }
    }

    public static final class TankFactory implements EnemyFactory {
        @Override public Enemy create(double x) { return new Tank(x); }
        @Override public String label() { return "TankFactory"; }
    }

    public static final class BeanFactory implements EnemyFactory {
        @Override public Enemy create(double x) { return new Bean(x); }
        @Override public String label() { return "BeanFactory"; }
    }
}
