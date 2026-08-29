package id.ac.ui.oop.s01pong;

/**
 * SESSION 1 — the Singleton pattern.
 *
 * The score is needed in three unrelated places: the ball (which scores),
 * the HUD (which draws it) and the game (which ends a rally). Passing one
 * ScoreManager through every constructor is the "object drilling" problem;
 * a single global access point removes it.
 *
 * Read the criticism in the class comment of {@link #getInstance()} too —
 * this pattern is taught WITH its costs, not as a free win.
 */
public final class ScoreManager {

    /** The one and only instance. Private and static: nobody else can hold one. */
    private static ScoreManager instance;

    private int playerScore;
    private int computerScore;
    private int rallies;

    /** Private constructor: `new ScoreManager()` is impossible from outside. */
    private ScoreManager() { }

    /**
     * The single global access point.
     *
     * COST OF THIS PATTERN (Session 1 discussion):
     *  - it is global mutable state, which makes tests order-dependent;
     *  - a class that calls getInstance() hides that dependency from its
     *    constructor, so you cannot tell what it needs by reading its API;
     *  - this simple version is not thread-safe.
     * We accept those costs here because the game is single-threaded and the
     * alternative (threading one object through every constructor) is worse.
     */
    public static ScoreManager getInstance() {
        if (instance == null) {
            instance = new ScoreManager();
        }
        return instance;
    }

    public void awardPlayer()   { playerScore++;   rallies++; }
    public void awardComputer() { computerScore++; rallies++; }

    public int playerScore()   { return playerScore; }
    public int computerScore() { return computerScore; }
    public int rallies()       { return rallies; }

    /** Used by the tests and by "restart"; keeps the field private. */
    public void reset() {
        playerScore = 0;
        computerScore = 0;
        rallies = 0;
    }
}
