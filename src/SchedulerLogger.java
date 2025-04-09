import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SchedulerLogger {
    private static final String BASE_LOG_PATH = "schedulerLog.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static Map<String, String> fireEvents = new HashMap<>();


    private static long millis;


    public static void clearLogFile() {
        File logFile = new File(BASE_LOG_PATH);
        if (logFile.exists()) {
            try (FileWriter writer = new FileWriter(logFile, false)) {
                writer.write("");
            } catch (IOException e) {
            }
        }
    }

    public static void logEventGivenTimestamp(String timestamp, String event, FireEvent f){
        String logEntry = String.format("[%s] %s | %s%n", timestamp, event, f);
        String logFilePath = BASE_LOG_PATH;

        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(logEntry);
        } catch (IOException e) {
        }
    }

    public static void logEventTimeDifference(String timeStart, String timeEnd, FireEvent f){
        String logFilePath = BASE_LOG_PATH;

        LocalTime startTime = LocalTime.parse(timeStart, formatter);
        LocalTime endTime = LocalTime.parse(timeEnd, formatter);

        // Calculate the duration
        Duration duration = Duration.between(startTime, endTime);
        millis = duration.toMillis();

        try (FileWriter writer = new FileWriter(logFilePath, true)) {

            if(millis != 0){
                writer.write(f + " put out in " + millis + " ms\n");
                millis = 0;
            }
        } catch (IOException e) {
        }
    }

    public static void logEvent(String event, FireEvent f) {
        String logEntry;
        String logFilePath = BASE_LOG_PATH;


        String timestamp = LocalDateTime.now().format(formatter);
        logEntry = String.format("[%s] %s | %s%n", timestamp, event, f);

        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(logEntry);

        } catch (IOException e) {
        }
    }


}
