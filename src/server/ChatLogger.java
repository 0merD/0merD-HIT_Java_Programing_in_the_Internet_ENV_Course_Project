package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;

/**
 * Handles all file logging operations for chat sessions.
 */
public class ChatLogger {
    private static final Path PROJECT_ROOT = Paths.get(System.getProperty("user.dir"));
    private static final Path LOGS_DIR = PROJECT_ROOT.resolve("logs");
    private static final Path LOG_FILE = LOGS_DIR.resolve("chat_log.txt");
    private static final Object LOG_LOCK = new Object(); // guard concurrent file writes

    static {
        try {
            if (!Files.exists(LOGS_DIR)) {
                Files.createDirectories(LOGS_DIR);
                System.out.println("Created logs directory: " + LOGS_DIR.toAbsolutePath());
            }
            if (!Files.exists(LOG_FILE)) {
                Files.createFile(LOG_FILE);
                System.out.println("Created log file: " + LOG_FILE.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize logging: " + e.getMessage());
        }
    }

    public static void saveSessionToLog(ChatSession session) {
        if (session.endTime == null) session.endSession();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Use friendly names (username@branch) instead of IPs
        String c1 = session.getClient1().getName();
        String c2 = session.getClient2().getName();

        StringBuilder entry = new StringBuilder();
        entry.append("=== Chat Session ===\n");
        entry.append("Participants: ").append(c1).append(" <-> ").append(c2).append("\n");
        entry.append("Start: ").append(df.format(session.startTime)).append("\n");
        entry.append("End:   ").append(df.format(session.endTime)).append("\n");
        entry.append("SavedContent: ").append(session.saveChatLog ? "yes" : "no").append("\n");
        if (session.saveChatLog) {
            entry.append("--- Transcript Start ---\n");
            entry.append(session.chatContent);
            entry.append("--- Transcript End ---\n");
        }
        entry.append("\n");

        synchronized (LOG_LOCK) {
            try (BufferedWriter writer = Files.newBufferedWriter(LOG_FILE, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                writer.write(entry.toString());
            } catch (IOException e) {
                System.err.println("Error writing chat log: " + e.getMessage());
            }
        }
    }

    // ... existing code ...
}