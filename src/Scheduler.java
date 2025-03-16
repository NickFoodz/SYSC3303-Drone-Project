/**
 * Class Scheduler models a scheduler to receive fire events and dispatch drones
 * @version 1.0
 * @author Nick Fuda
 */

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Scheduler {
    private FireEvent current;
    private boolean shutdownFIS;
    private boolean shutdownDrones;
    private Simulation simulation;
    private ArrayList<FireEvent> eventList = new ArrayList<>();

    private DatagramSocket sendSocket, receiveSocket, acceptSocket;

    /**
     * Constructor for scheduler class
     */
    public Scheduler(){
        eventList = new ArrayList<>();
        current = null;
        shutdownFIS = false;
        shutdownDrones = false;
        this.simulation = simulation;
        try {
            sendSocket = new DatagramSocket(6000);
            receiveSocket = new DatagramSocket(6001);
            acceptSocket = new DatagramSocket(6002);
        } catch (SocketException se) {
            se.printStackTrace();
        }
    }

    public void initializeSendReceiveThreads(){
        Thread sendThread = new Thread(new SchedulerSend(this));
        Thread recThread = new Thread(new SchedulerReceive(this));
        sendThread.start();
        recThread.start();

    }


    /**
     * Used by FireIncidentSystem to signal to scheduler that it has completed its data cycle.
     */
    public synchronized void setShutdownFIS(){
        shutdownFIS = true;
    }

    /**
     * Allows drone subsystem to check if everything is shutdown
     * @return
     */
    public boolean getShutdownDrones(){return shutdownDrones;}

    /**
     * Gets events from the FIS and messages from the Drones
     */
    public void receiveMessage(){
        //    public FireEvent(String time, int zoneID, String type, String severity){
        byte[] rec= new byte[100];
        DatagramPacket receivePacket = new DatagramPacket(rec, rec.length);
        try {

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
                    System.out.println("Received: " + newEvent +" from Fire Incident Subsystem");
                    addEvent(newEvent);
                }

                //Otherwise if from a drone
            }else if(receivePacket.getPort() == (5001 | 5002 | 5003)){

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
                if(msg.equalsIgnoreCase("Accepted")){
                    notifyAcceptance(current);
                }
                //Acknowledge acceptance and remove task from list
                System.out.println("Drone accepted Task\n");
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
     * Notify all processes that an event was handled, clearing the scheduler for a new event
     * @param event the event that will be reported as complete
     */
    public void notifyAcceptance(FireEvent event){
        System.out.println("Event being handled: " + event);
        if(!eventList.isEmpty()){
            current = eventList.getFirst();
        } else {
            current = null;
        }

        notifyAll();
    }

    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler();
        scheduler.initializeSendReceiveThreads();
        System.out.println("Scheduler Online");
        System.out.println("Receive Socket Awaiting Message...\n");
        while(true){
        }
    }


//    @Override
//    public void run() {
//        while(!shutdownFIS && !Thread.currentThread().isInterrupted()){
//            try {
//                receiveMessage();
//                //sendToDrone();
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                break;
//            }
//        }
//        //prepare to shutdown after last data read from csv
//        shutdown();
//        System.out.println("Shutting down Scheduler");
//    }

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
                    System.out.println("No Tasks to Send");
                };
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {}
            }
        }
    }


}
