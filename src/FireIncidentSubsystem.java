/**
 * Class FireIncidentSubsystem reads event csv files and creates a new FireEvent to send to the scheduler
 * @version 1.0
 * @author Nick Fuda
 */

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FireIncidentSubsystem implements Runnable {
    private final Scheduler scheduler; //Scheduler
    private final Simulation simulation; //Simulation **NEEDED**
    private final String eventFilePath = "Sample_event_file.csv"; //File path to the .csv
    boolean EOF; //end of file reached
    List<FireEvent> allEvents;
    private DatagramSocket FISSocket;


    /**
     * Constructor
     *
     * @param scheduler the scheduler
     */
    public FireIncidentSubsystem(Scheduler scheduler, Simulation simulation) {
        this.scheduler = scheduler;
        this.simulation = simulation;
        EOF = false;
        allEvents = new ArrayList<>();
        try {
            FISSocket = new DatagramSocket(5999);
        } catch (SocketException e) {}
        simulation.setFireIncidentSubsystem(this);
    }

    /**
     * Reads the data from a csv and puts it into a master AllEvents List
     */
    public void getData() {
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
                    String eventStr = event.summarizeEvent();
                    sendToScheduler(eventStr);
                    System.out.println("Fire Incident Subsystem Sent: " + event);
                    //Checks the format of the event for the drone in UDP
                    //System.out.println(event.summarizeEvent());

                    //adds all events to a master AllEvents List
                    allEvents.add(event);
                }
            }

            //sorts all events by timestamp so we can send them out to the simulation in order.
            allEvents.sort(Comparator.comparing(FireEvent::getTime));


        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        EOF = true;
    }

    private void sendToScheduler(String eventInfo){
        byte[] outgoingEvent = new byte[100];
        outgoingEvent = eventInfo.getBytes();

        try {
            DatagramPacket outgoing = new DatagramPacket(outgoingEvent, eventInfo.length(), InetAddress.getLocalHost(), 6001);
            FISSocket.send(outgoing);
        } catch (IOException e) {
            System.out.println("Could not send event");
        }

    }

    /**
     * Checks if theres an event to be send out at the current simulation time
     * Then removes it from the master AllEvents list and sends it out to the scheduler.
     **/
    public synchronized void consumeEvent(LocalTime currentSimTime) {
        while (!allEvents.isEmpty() && allEvents.get(0).getTime().equals(currentSimTime)) {
            FireEvent event = allEvents.remove(0);
            //SENDING EVENT TO SCHEDULER:
            scheduler.addEvent(event);
        }

        if(allEvents.isEmpty()){
            notifyAll();
        }
    }

    /**
     * Overrides the run function in Runnable
     * Reads in FireEvents from file. Then wait for simulation to trigger events. Shutdown after all events are consumed
     */
    @Override
    public void run() {
        getData();

        synchronized (this) {
            while (!allEvents.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    //*FOR JUNIT TESTS*
                    System.out.println("FireIncidentSubsystem thread interrupted, shutting down.");
                    Thread.currentThread().interrupt(); // Restore interrupt status
                    break;
                }
            }
        }
        scheduler.setShutdownFIS();
        System.out.println("Fire Incident Subsystem Shutting Down");
    }

}
