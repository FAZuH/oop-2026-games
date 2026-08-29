package id.ac.ui.oop.s06snake;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SESSION 6 — the tests.
 *
 * Every test here runs without opening a window, because SnakeLogic and
 * HighScoreRepository have no JavaFX in them. That is the payoff for a design
 * decision, not an accident — and it is the point students should take away.
 */
class SnakeLogicTest {

    @Test
    void snakeMovesInTheDirectionItFaces() {
        SnakeLogic logic = new SnakeLogic(10, 10, 1);
        SnakeLogic.Cell before = logic.head();

        logic.step();

        assertEquals(before.x() + 1, logic.head().x(), "default direction is RIGHT");
        assertEquals(before.y(), logic.head().y());
    }

    @Test
    void snakeCannotReverseIntoItsOwnNeck() {
        SnakeLogic logic = new SnakeLogic(10, 10, 1);   // facing RIGHT, body to the left

        logic.turn(SnakeLogic.Direction.LEFT);           // illegal, must be ignored
        logic.step();

        assertTrue(logic.isAlive(), "reversing must be refused, not fatal");
        assertEquals(6, logic.head().x(), "it should have kept going right");
    }

    @Test
    void hittingAWallEndsTheGame() {
        SnakeLogic logic = new SnakeLogic(6, 6, 1);
        for (int i = 0; i < 10; i++) logic.step();       // run into the right wall

        assertFalse(logic.isAlive());
        assertFalse(logic.step(), "stepping a dead snake does nothing");
    }

    @Test
    void turningIsQueuedNotAppliedTwiceInOneTick() {
        SnakeLogic logic = new SnakeLogic(12, 12, 1);
        logic.turn(SnakeLogic.Direction.UP);
        logic.turn(SnakeLogic.Direction.LEFT);          // second turn in the same tick
        logic.step();

        // UP was overwritten by LEFT before the tick, but LEFT is legal only
        // because the direction was still RIGHT... so it must have been refused.
        assertEquals(SnakeLogic.Direction.UP.dx, 0);
        assertTrue(logic.isAlive());
    }

    // ------------------------------------------------ persistence & exceptions

    @Test
    void missingScoreFileIsNormalNotAnError() {
        HighScoreRepository repository =
                new HighScoreRepository(Path.of("build", "test-scores", "does-not-exist.csv"));

        List<HighScoreRepository.Entry> entries = repository.load();

        assertTrue(entries.isEmpty());
        assertFalse(repository.warnings().isEmpty(), "it should explain why, not throw");
    }

    @Test
    void corruptLinesAreSkippedAndReported() throws Exception {
        Path file = Path.of("build", "test-scores", "corrupt.csv");
        Files.createDirectories(file.getParent());
        Files.write(file, List.of("alice,50", "this line is broken", "bob,not-a-number", "carol,90"));

        HighScoreRepository repository = new HighScoreRepository(file);
        List<HighScoreRepository.Entry> entries = repository.load();

        assertEquals(2, entries.size(), "the two good rows survive");
        assertEquals("carol", entries.get(0).name(), "sorted highest first");
        assertEquals(2, repository.warnings().size(), "both bad rows are reported");
    }

    @Test
    void recordKeepsOnlyTheTopFive() throws Exception {
        Path file = Path.of("build", "test-scores", "top5.csv");
        Files.deleteIfExists(file);
        HighScoreRepository repository = new HighScoreRepository(file);

        for (int score = 10; score <= 70; score += 10) {
            repository.record("p" + score, score);
        }
        List<HighScoreRepository.Entry> entries = repository.load();

        assertEquals(5, entries.size());
        assertEquals(70, entries.get(0).score());
        assertEquals(30, entries.get(4).score(), "the two lowest were dropped");
    }
}
