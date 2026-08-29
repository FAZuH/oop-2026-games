package id.ac.ui.oop.s05breakout;

import java.util.List;

/**
 * SESSION 5 — the Facade pattern.
 *
 * Starting a level really means: pick a loader, run its template method, reset
 * the ball and paddle, zero the score, and note which level we are on. The
 * screen class should not have to know that sequence — it should be able to say
 * `engine.startLevel(1)` and get on with drawing.
 *
 * A Facade does not add behaviour. It gives a messy set of steps ONE sensible
 * front door. Its risk, worth naming in class: a facade that keeps growing
 * becomes a God object that knows about everything.
 */
public class GameEngine {

    private final LevelLoader[] loaders = { new Levels.Classic(), new Levels.Pyramid() };

    private LevelLoader.Level level;
    private int levelIndex;
    private int score;
    private int lives = 3;

    private double ballX, ballY, ballVX, ballVY;
    private double paddleX;

    private final double width, height;

    public GameEngine(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /** The one call the UI needs. Everything below is hidden behind it. */
    public void startLevel(int index) {
        levelIndex = Math.floorMod(index, loaders.length);
        level = loaders[levelIndex].load();     // <- the template method runs here
        resetBallAndPaddle();
    }

    public void nextLevel() { startLevel(levelIndex + 1); }

    private void resetBallAndPaddle() {
        paddleX = width / 2;
        ballX = width / 2;
        ballY = height - 90;
        ballVX = level.ballSpeed() * 0.55;
        ballVY = -level.ballSpeed();
    }

    public void movePaddle(double targetX, double dt) {
        double step = 520 * dt;
        if (Math.abs(targetX - paddleX) < step) paddleX = targetX;
        else paddleX += targetX > paddleX ? step : -step;
        paddleX = Math.max(56, Math.min(width - 56, paddleX));
    }

    public void update(double dt) {
        ballX += ballVX * dt;
        ballY += ballVY * dt;

        if (ballX < 8)            { ballX = 8;             ballVX = -ballVX; }
        if (ballX > width - 8)    { ballX = width - 8;     ballVX = -ballVX; }
        if (ballY < 8)            { ballY = 8;             ballVY = -ballVY; }

        // paddle
        if (ballY > height - 46 && ballY < height - 30
                && Math.abs(ballX - paddleX) < 56 && ballVY > 0) {
            ballVY = -Math.abs(ballVY);
            ballVX += (ballX - paddleX) * 2.2;
        }

        if (ballY > height + 20) {          // lost the ball
            lives--;
            resetBallAndPaddle();
        }

        for (Brick brick : level.bricks()) {
            if (brick.isAlive() && brick.intersects(ballX, ballY, 8)) {
                brick.hit();
                score += 10;
                ballVY = -ballVY;
                break;                       // one brick per frame keeps it fair
            }
        }

        if (level.bricks().stream().noneMatch(Brick::isAlive)) nextLevel();
    }

    public List<Brick> bricks()   { return level.bricks(); }
    public String levelName()     { return level.name(); }
    public int levelNumber()      { return levelIndex + 1; }
    public double ballX()         { return ballX; }
    public double ballY()         { return ballY; }
    public double paddleX()       { return paddleX; }
    public int score()            { return score; }
    public int lives()            { return lives; }
}
