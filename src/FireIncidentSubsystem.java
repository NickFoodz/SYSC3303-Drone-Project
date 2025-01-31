/**
 * Class FireIncidentSubsystem reads event csv files and creates a new FireEvent to send to the scheduler
 * @version 1.0
 * @author Nick Fuda
 */

import java.io.*;

public class FireIncidentSubsystem implements Runnable {
    private final Scheduler scheduler; //Scheduler
    private final String eventFilePath; //File path to the .csv
    boolean EOF = false;

    /**
     * Constructor
     * @param scheduler the scheduler
     * @param eventFilePath the path to the .csv
     */
    public FireIncidentSubsystem(Scheduler scheduler, String eventFilePath){
        this.scheduler = scheduler;
        this.eventFilePath = eventFilePath;
    }

    /**
     * Reads the data from a csv and puts it into the scheduler
     */
    public synchronized void getData() {
        //Try to read the csv
        try (BufferedReader reader = new BufferedReader(new FileReader(eventFilePath))) {
            String line;
            reader.readLine(); //First line is header

            //While there is data on the lines, create a new fire event and add it to the scheduler
            while ((line = reader.readLine()) != null) {
                String[] info = line.split(",");
                if (info.length == 4) {
                    String time = info[0];
                    int zoneID = Integer.parseInt(info[1]);
                    String type = info[2];
                    String severity = info[3];
                    FireEvent event = new FireEvent(time, zoneID, type, severity);
                    System.out.println("Fire Incident Subsystem Sent: " + event);
                    scheduler.addEvent(event);
                    Thread.sleep(1000);
                }
            }

        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        EOF = true;
    }

        /**
     * Overrides the run function in Runnable
     */
    @Override
    public void run() {
            getData();
    }
}
