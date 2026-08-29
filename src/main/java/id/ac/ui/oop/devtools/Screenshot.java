package id.ac.ui.oop.devtools;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.io.File;

/**
 * TEST SCAFFOLDING — not course material.
 *
 * Lets any session game render for a moment, save a PNG of its canvas and
 * exit on its own, so every game can be verified without a human watching:
 *
 *     ./gradlew shotAll        -> build/shots/S01.png ... S06.png
 *
 * When the -Dshot property is absent (the normal case for a lecture demo)
 * this class does nothing at all and the game just runs.
 */
public final class Screenshot {

    private Screenshot() { }

    /** True when the JVM was started with -Dshot=<path>. */
    public static boolean requested() {
        return System.getProperty("shot") != null;
    }

    /** Seconds to run before capturing; -Dshot.after=2.0, default 1.5s. */
    public static double after() {
        try {
            return Double.parseDouble(System.getProperty("shot.after", "1.5"));
        } catch (NumberFormatException e) {
            return 1.5;
        }
    }

    /**
     * Saves the canvas to the path given by -Dshot and quits the application.
     * Prints a single machine-readable line so CI can grep for success.
     */
    public static void captureAndExit(Canvas canvas, String label) {
        try {
            File out = new File(System.getProperty("shot")).getAbsoluteFile();
            File parent = out.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("cannot create " + parent);
            }
            WritableImage image = new WritableImage(
                    (int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, image);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", out);
            System.out.println("SHOT-OK " + label + " -> " + out);
        } catch (Exception e) {
            System.out.println("SHOT-FAIL " + label + " : " + e);
        } finally {
            Platform.exit();
        }
    }
}
