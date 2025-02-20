/**
 * Class FireIncidentSubsystem reads event csv files and creates a new FireEvent to send to the scheduler
 * @version 1.0
 * @author Nick Fuda
 */

import java.io.*;
import java.util.ArrayList;

public class FireIncidentSubsystem implements Runnable {
    private final Scheduler scheduler; //Scheduler
    private final String eventFilePath = "Sample_event_file.csv"; //File path to the .csv
    boolean EOF; //end of file reached


    /**
     * Constructor
     * @param scheduler the scheduler
     */
    public FireIncidentSubsystem(Scheduler scheduler){
        this.scheduler = scheduler;
        EOF = false;
    }

    /**
     * Reads the data from a csv and puts it into the scheduler
     */
    public void getData() {
        //Try to read the csv
        try (BufferedReader reader = new BufferedReader(new FileReader(eventFilePath))) {
            String line;
            reader.readLine(); //First line is header
            ArrayList<FireEvent> eventsToBeSorted = new ArrayList<>();

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
                    eventsToBeSorted.add(event);
                    Thread.sleep(1000);

                }
            }

            //sort
            sortEvents(eventsToBeSorted);

            for(int i = 0; i < eventsToBeSorted.size(); i++){
                scheduler.addEvent(eventsToBeSorted.get(i));
            }

        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        EOF = true;
        scheduler.setShutdownFIS();
    }

    /**
     * Sorts all the events by severity level
     * @param events the unsorted event list
     */
    private void sortEvents(ArrayList<FireEvent> events){
        //sort the events
        for (int i = 0; i < events.size() - 1; i++) {
            int compId = i;
            for (int j = i + 1; j < events.size(); j++) {
                if (events.get(j).getSeverityLevel() > events.get(compId).getSeverityLevel()) {
                    compId = j;
                }
            }

            //event swap
            FireEvent temp = events.get(i);
            events.set(i, events.get(compId));
            events.set(compId, temp);
        }
    }

        /**
     * Overrides the run function in Runnable
     */
    @Override
    public void run() {
            getData();
            System.out.println("Shutting down FIS");

    }
}
