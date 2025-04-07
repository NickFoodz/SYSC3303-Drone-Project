import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DroneLogger {
    private static final String BASE_LOG_PATH = "";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static String getLogFilePath(int droneNum) {
        return "drone" + droneNum + "_logs.txt";
    }

    public static void clearLogFile(int droneNum) {
        File logFile = new File(getLogFilePath(droneNum));
        if (logFile.exists()) {
            try (FileWriter writer = new FileWriter(logFile, false)) {
                writer.write("");
                System.out.println("Log file cleared for drone" + droneNum);
            } catch (IOException e) {
                System.err.println("Error clearing log file: " + e.getMessage());
            }
        } else {
            System.out.println("Log file for drone" + droneNum + " does not exist. Nothing to clear");
        }
    }

    public static void logEvent(String event, int droneNum) {
        String logEntry;
        String logFilePath = getLogFilePath(droneNum);


        String timestamp = LocalDateTime.now().format(formatter);
        logEntry = String.format("[%s] %s%n", timestamp, event);


        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(logEntry);
        } catch (IOException e) {
        }
    }
}
