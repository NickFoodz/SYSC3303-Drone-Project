/**
 * Class Scheduler models a scheduler to receive fire events and dispatch drones
 * @version 1.0
 * @author Nick Fuda
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class Scheduler {
    private FireEvent current;
    private LinkedList<FireEvent> eventList;
    public int mostRecentReceivedSocket = 0;
    private DatagramSocket sendSocket, receiveSocket, acceptSocket;
    public ArrayList<String> log;
    List<Zone> allZones;

    public Boolean TESTING_rejectionHandled;

    /**
     * Constructor for scheduler class
     */
    public Scheduler(){
        eventList = new LinkedList<>();
        current = null;
        log = new ArrayList<>();
        //Set up the sockets
        try {
            sendSocket = new DatagramSocket(6000);
            receiveSocket = new DatagramSocket(6001);
            acceptSocket = new DatagramSocket(6002);
        } catch (SocketException se) {
            se.printStackTrace();
        }
        TESTING_rejectionHandled = false;
    }


    /**
     * Initializes the threads to send and receive messages
     */
    public void initializeSendReceiveThreads(){
        //Create threads
        Thread sendThread = new Thread(new SchedulerSend(this));
        Thread recThread = new Thread(new SchedulerReceive(this));
        //Start the threads
        sendThread.start();
        recThread.start();
    }

    public void TESTING_closeSockets(){
        sendSocket.close();
        receiveSocket.close();
        acceptSocket.close();
    }

    /**
     * Gets events from the FIS and messages from the Drones
     */
    public void receiveMessage(){
        //Constructor for  FireEvent(String time, int zoneID, String type, String severity){
        //Create the buffer and datagram packets
        byte[] rec= new byte[100];
        DatagramPacket receivePacket = new DatagramPacket(rec, rec.length);
        try {
            //Receive messages and convert to strings
            receiveSocket.receive(receivePacket);
            String recMsg = new String (receivePacket.getData(), 0, receivePacket.getLength());
            mostRecentReceivedSocket = receivePacket.getPort();
            //If info is from FIS
            if(receivePacket.getPort() == 5999){
                //Convert into FireEvent
                String[] info = recMsg.split(",");
                if (info.length == 6) {
                    String time = info[0];
                    int zoneID = Integer.parseInt(info[1]);
                    String type = info[2];
                    String severity = info[3];
                    String fault = info[4];
                    FireEvent newEvent = new FireEvent(time, zoneID, type, severity, fault);

                    //Print that the event was received with its info
                    System.out.println("Received: " + newEvent +" from Fire Incident Subsystem");
                    //Add to the event queue
                    addEvent(newEvent);
                }

                //Otherwise if from a drone print it directly to the terminal
            }else if(receivePacket.getPort() == 5001 || receivePacket.getPort() == 5002 || receivePacket.getPort() == 5003){
                System.out.println("\nNew message from Drone on port " + receivePacket.getPort());
                System.out.print(recMsg + "\n");
            }
        } catch (IOException e) { System.out.println("Error Scheduler Receiving");}
    }

    /**
     * Sends DroneSubsystem a UDP summary of the event
     */
    public void sendToDrone(){
        if(!eventList.isEmpty()){
            current = eventList.getFirst();
            byte[] outData = new byte[100];
            String eventString = current.summarizeEvent();
            outData = eventString.getBytes();

            //iter4 fault handling
            String fault = current.getFault();
            if (!fault.equals("null")) {
                System.out.println("A Fault has been detected: " + fault);
                switch (fault) {
                    case "Drone Stuck":
                        System.out.println("A Drone Stuck fault will be injected and handled to simulate a drone being stuck between zones mid-flight.");
                        log.add("Drone Stuck");
                        break;
                    case "Nozzle Jammed":
                        System.out.println("A Nozzle Jammed fault will be injected and handled to simulate the nozzle/bay doors being jammed.");
                        log.add("Nozzle Jammed");
                        break;
                    case "Packet Loss/Corrupted Messages":
                        System.out.println("A Packet Loss/Corrupted Messages fault will be injected and handled.");
                        log.add("Packet Loss/Corrupted Messages");
                        break;
                }
            }


            try {
                System.out.println("Attempting to send Event to Drone");
                //Can change from a localhost if needed on multiple computers
                DatagramPacket sendEvent = new DatagramPacket(outData, eventString.length(),InetAddress.getLocalHost(), 5000);
                //Try sending to Drone subsystem
                sendSocket.send(sendEvent);

                //Wait for accepted message on accept socket
                byte[] acceptData = new byte[100];
                DatagramPacket reply = new DatagramPacket(acceptData, acceptData.length);

                acceptSocket.receive(reply);
                mostRecentReceivedSocket = reply.getPort();
                String msg = new String(reply.getData(), 0, reply.getLength());
                if(msg.equalsIgnoreCase("ACCEPT")){
                    notifyAcceptance(current);
                    //Acknowledge acceptance and remove task from list
                    System.out.println("Drone accepted task, delegating next\n");
                    eventList.remove(0);
                    current = eventList.getFirst();
                }
                if(msg.equalsIgnoreCase("REJECT")){
                    current.clearFault();
                    notifyAcceptance(current); //Maybe remove? or at least change name. Gonna leave in for niceness in console
                    System.out.println("Drone did not accept task\n");
                    current = eventList.getFirst();
                    TESTING_rejectionHandled = true;
                }



            } catch (IOException e) {}
        }
    }

    /**
     * Adds an event to the scheduler
     * @param event the event to be added
     */
    public void addEvent(FireEvent event){

        //puts HIGH severity FireEvents at the front of the queue, and the rest at the back
        System.out.println("Event added: " + event +"\n");
        if(event.getSeverity().equals("High")){
            eventList.addFirst(event);
        } else {
            eventList.addLast(event);
        }
        current = eventList.getFirst();
    }

    /**
     * Returns the event list stored in the scheduler
     */
    public LinkedList<FireEvent> getEvent() {
        return eventList;
    }


    /**
     * Returns the current event of the scheduler
     */
    public FireEvent getCurrentEvent() {
        return current;
    }

    /**
     * Notify all processes that an event was handled, clearing the scheduler for a new event
     * @param event the event that will be reported as complete
     */
    public void notifyAcceptance(FireEvent event){
        System.out.println("Event being handled: " + event);
    }

    /**
     * Main method that runs the scheduler
     * @param args none
     */
    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler();
        scheduler.initializeSendReceiveThreads();
        System.out.println("Scheduler Online");
        System.out.println("Receive Socket Awaiting Message...\n");
        while(true){
        }
    }


    /**
     * Helper class that allows packet receiving on its own thread
     * to prevent deadlock
     */
    private class SchedulerReceive implements Runnable{
        Scheduler s;
        public SchedulerReceive(Scheduler scheduler){
            this.s = scheduler;
        }

        @Override
        public void run() {
            while(true){
                s.receiveMessage();
            }
        }
    }

    /**
     * Helper class that allows packet sending on its own thread
     * to prevent deadlock
     */
    private class SchedulerSend implements Runnable{

        Scheduler s;

        public SchedulerSend(Scheduler scheduler){
            this.s = scheduler;
        }

        @Override
        public void run() {
            while(true) {
                try{
                    s.sendToDrone();
                } catch (NoSuchElementException e){
                    System.out.println("No pending tasks to send\n");
                }
                try {
                    Thread.sleep(1000);
                    System.out.println("Waiting to send task\n");

                } catch (InterruptedException e) {}
            }
        }
    }


}
