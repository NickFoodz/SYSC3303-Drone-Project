/**
 * Class FireIncidentSubsystem reads event csv files and creates a new FireEvent to send to the scheduler
 * @version 1.0
 * @author Nick Fuda
 */

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FireIncidentSubsystem {
    private final String eventFilePath = "Sample_event_file.csv"; //File path to the .csv
    boolean EOF; //end of file reached
    List<FireEvent> allEvents;
    private static Simulation simulation;
    private DatagramSocket FISSocket;


    /**
     * Constructor
     *
     */
    public FireIncidentSubsystem() {
        EOF = false;
        allEvents = new ArrayList<>();
        simulation = new Simulation(1, this);
        try {
            FISSocket = new DatagramSocket(5999);
        } catch (SocketException e) {}

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

                    //Checks the format of the event for the drone in UDP
                    //System.out.println(event.summarizeEvent());

                    //adds all events to a master AllEvents List
                    allEvents.add(event);
                }
            }

            //sorts all events by timestamp so we can send them out to the simulation in order.
            allEvents.sort(Comparator.comparing(FireEvent::getTime));
            System.out.println(allEvents);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        EOF = true;
    }

    /**
     * Use UDP and IP to send the extracted events from the csv to the scheduler
     * @param eventInfo
     */
    private void sendToScheduler(String eventInfo){
        byte[] outgoingEvent = new byte[100];
        outgoingEvent = eventInfo.getBytes();

        try {
            DatagramPacket outgoing = new DatagramPacket(outgoingEvent, eventInfo.length(), InetAddress.getLocalHost(), 6001);
            FISSocket.send(outgoing);
            System.out.println("Fire Incident Subsystem Sent: " + eventInfo +"\n");
        } catch (IOException e) {
            System.out.println("Could not send event");
        }

    }

    /**
     * Checks if theres an event to be send out at the current simulation time
     * Then removes it from the master AllEvents list and sends it out to the scheduler.
     * @param currentSimTime
     */
    public synchronized void consumeEvent(LocalTime currentSimTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        while (!allEvents.isEmpty() && allEvents.get(0).getTime().equals(currentSimTime.format(formatter))) {
            System.out.println(allEvents);
            FireEvent event = allEvents.remove(0);
            //SENDING EVENT TO SCHEDULER:
            String eventStr = event.summarizeEvent();
            sendToScheduler(eventStr);
        }

        if(allEvents.isEmpty()){
            notifyAll();
            simulation.endSimulation();
        }
    }


    /**
     * Main method to run  FIS
     * @param args
     */
    public static void main(String[] args) {
        FireIncidentSubsystem fis = new FireIncidentSubsystem();
        System.out.println("Fire Incident Subsystem Online\n");
        fis.getData();
        Thread simulationThread = new Thread(simulation);
        simulationThread.start();
        while(true){
        //exist (wait for shutdown signal)
        }
    }
}
