/**
 * Class Scheduler models a scheduler to receive fire events and dispatch drones
 * @version 1.0
 * @author Nick Fuda
 */

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Scheduler {
    private FireEvent current;
    private LinkedList<FireEvent> eventList = new LinkedList<>();

    private DatagramSocket sendSocket, receiveSocket, acceptSocket;

    /**
     * Constructor for scheduler class
     */
    public Scheduler(){
        eventList = new LinkedList<>();
        current = null;

        //Set up the sockets
        try {
            sendSocket = new DatagramSocket(6000);
            receiveSocket = new DatagramSocket(6001);
            acceptSocket = new DatagramSocket(6002);
        } catch (SocketException se) {
            se.printStackTrace();
        }
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

            //If info is from FIS
            if(receivePacket.getPort() == 5999){
                //Convert into FireEvent
                String[] info = recMsg.split(",");
                if (info.length == 4) {
                    String time = info[0];
                    int zoneID = Integer.parseInt(info[1]);
                    String type = info[2];
                    String severity = info[3];
                    FireEvent newEvent = new FireEvent(time, zoneID, type, severity);

                    //Print that the event was received with its info
                    System.out.println("Received: " + newEvent +" from Fire Incident Subsystem");
                    //Add to the event queue
                    addEvent(newEvent);
                }

                //Otherwise if from a drone print it directly to the terminal
            }else if(receivePacket.getPort() == (5001 | 5002 | 5003)){
                System.out.println("\nNew message from Drone: ");
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
                String msg = new String(reply.getData(), 0, reply.getLength());
                if(msg.equalsIgnoreCase("ACCEPT")){
                    notifyAcceptance(current);
                }
                //Acknowledge acceptance and remove task from list
                System.out.println("Drone accepted task, delegating next\n");
                eventList.remove(0);
                current = eventList.getFirst();

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
        eventList.remove(event);
        if(!eventList.isEmpty()){
            current = eventList.getFirst();
        } else {
            current = null;
        }
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
                };
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {}
            }
        }
    }


}
