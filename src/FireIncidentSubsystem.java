import java.io.*;
import java.util.*;

public class FireIncidentSubsystem implements Runnable {
    private final Scheduler scheduler;
    private final String eventFilePath;

    public FireIncidentSubsystem(Scheduler scheduler, String eventFilePath){
        this.scheduler = scheduler;
        this.eventFilePath = eventFilePath;
    }

    @Override
    public void run() {
        try(BufferedReader reader = new BufferedReader(new FileReader(eventFilePath))){
            String line;
            reader.readLine(); //First line is header

            while((line = reader.readLine()) != null){
                String[] info = line.split(",");
                if(info.length == 4){
                    String time = info[0];
                    int zoneID = Integer.parseInt(info[1]);
                    String type = info[2];
                    String severity = info[3];
                    FireEvent event = new FireEvent(time, zoneID, type, severity);
                    //Add to scheduler
                    System.out.println(event);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
