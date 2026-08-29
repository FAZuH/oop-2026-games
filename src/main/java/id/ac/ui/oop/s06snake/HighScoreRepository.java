package id.ac.ui.oop.s06snake;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * SESSION 6 — file I/O and exception design.
 *
 * THE DESIGN QUESTION THIS CLASS ANSWERS
 *   A high-score file is not important enough to crash the game over. A missing
 *   file is normal (first run). A corrupt line is somebody editing the file by
 *   hand. Neither should ever reach the player as a stack trace.
 *
 *   So: {@link #load()} degrades gracefully and never throws, while
 *   {@link #save} DOES throw a checked {@link ScoreStoreException}, because a
 *   failed save is something the caller must decide about.
 *
 *   That asymmetry is the lesson: the right exception policy depends on whether
 *   the caller can do anything useful about the failure.
 */
public class HighScoreRepository {

    /** A checked exception: saving failed and the caller has to deal with it. */
    public static class ScoreStoreException extends Exception {
        public ScoreStoreException(String message, Throwable cause) { super(message, cause); }
    }

    public record Entry(String name, int score) { }

    private final Path file;
    private final List<String> warnings = new ArrayList<>();

    public HighScoreRepository(Path file) {
        this.file = file;
    }

    /**
     * Never throws. A missing file yields an empty list; unreadable lines are
     * skipped and reported through {@link #warnings()} so the UI can mention
     * them quietly instead of dying.
     */
    public List<Entry> load() {
        warnings.clear();
        List<Entry> entries = new ArrayList<>();
        if (!Files.exists(file)) {
            warnings.add("no score file yet — this looks like a first run");
            return entries;
        }
        try {
            // try-with-resources is unnecessary here: readAllLines closes for us.
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", 2);
                if (parts.length != 2) {
                    warnings.add("line " + (i + 1) + ": expected 'name,score'");
                    continue;
                }
                try {
                    entries.add(new Entry(parts[0].trim(), Integer.parseInt(parts[1].trim())));
                } catch (NumberFormatException e) {
                    warnings.add("line " + (i + 1) + ": '" + parts[1].trim() + "' is not a number");
                }
            }
        } catch (IOException e) {
            warnings.add("could not read the score file: " + e.getMessage());
        }
        entries.sort(Comparator.comparingInt(Entry::score).reversed());
        return entries;
    }

    /** Throws, because a failed save is worth telling the caller about. */
    public void save(List<Entry> entries) throws ScoreStoreException {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            List<String> lines = new ArrayList<>();
            for (Entry entry : entries) lines.add(entry.name() + "," + entry.score());
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ScoreStoreException("could not write " + file, e);
        }
    }

    /** Records a score, keeps the top five, and persists. */
    public List<Entry> record(String name, int score) throws ScoreStoreException {
        List<Entry> entries = new ArrayList<>(load());
        entries.add(new Entry(name, score));
        entries.sort(Comparator.comparingInt(Entry::score).reversed());
        List<Entry> top = new ArrayList<>(entries.subList(0, Math.min(5, entries.size())));
        save(top);
        return top;
    }

    public List<String> warnings() { return List.copyOf(warnings); }
}
