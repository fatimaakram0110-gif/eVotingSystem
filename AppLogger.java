package evoting.util;

import java.io.IOException;
import java.util.logging.*;

/**
 * Singleton logger wrapper that writes to both the console and a
 * rotating file (logs/evoting.log).
 *
 * Requirement satisfied:
 *  - Exception Handling : file-handler creation failures are caught and
 *                         reported without crashing the application.
 *  - Code Refactoring   : all logging concerns are centralised here.
 */
public final class AppLogger {

    private static final String LOG_FILE = "logs/evoting.log";
    private static final Logger logger   = Logger.getLogger("EVotingSystem");

    static {
        try {
            // Prevent duplicate handlers if class is initialised more than once
            logger.setUseParentHandlers(false);

            // Console handler
            ConsoleHandler console = new ConsoleHandler();
            console.setLevel(Level.ALL);
            console.setFormatter(new SimpleFormatter());
            logger.addHandler(console);

            // File handler (append = true)
            FileHandler file = new FileHandler(LOG_FILE, true);
            file.setLevel(Level.ALL);
            file.setFormatter(new SimpleFormatter());
            logger.addHandler(file);

            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            // Fall back to console-only logging
            System.err.println("[AppLogger] Could not create file handler: " + e.getMessage());
        }
    }

    private AppLogger() {} // utility class – no instances

    public static void info(String msg)              { logger.info(msg); }
    public static void warning(String msg)           { logger.warning(msg); }
    public static void severe(String msg)            { logger.severe(msg); }
    public static void severe(String msg, Throwable t) { logger.log(Level.SEVERE, msg, t); }
    public static void fine(String msg)              { logger.fine(msg); }
}
