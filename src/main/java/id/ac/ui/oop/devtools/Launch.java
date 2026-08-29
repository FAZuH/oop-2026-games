package id.ac.ui.oop.devtools;

import javafx.application.Application;

import java.util.Arrays;

/**
 * TEST SCAFFOLDING — not course material.
 *
 * WHY THIS CLASS EXISTS (worth one sentence in the setup guide):
 * when the class you launch *extends* {@link Application}, the JVM refuses to
 * start unless the JavaFX modules are on the --module-path, and you get
 *
 *     Error: JavaFX runtime components are missing, and are required to run
 *
 * Launching from a class that does NOT extend Application skips that check, so
 * JavaFX loads from the ordinary classpath that Gradle already built for us.
 * That keeps every per-session run task working without a single --module-path
 * flag anywhere in the project.
 *
 * Usage (from build.gradle):  Launch &lt;fully.qualified.GameClass&gt; [args...]
 */
public final class Launch {

    private Launch() { }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("usage: Launch <fully.qualified.GameClass> [args...]");
            System.exit(2);
        }
        Class<?> gameClass = Class.forName(args[0]);
        if (!Application.class.isAssignableFrom(gameClass)) {
            System.err.println(args[0] + " is not a javafx.application.Application");
            System.exit(2);
        }
        Application.launch((Class<? extends Application>) gameClass,
                Arrays.copyOfRange(args, 1, args.length));
    }
}
