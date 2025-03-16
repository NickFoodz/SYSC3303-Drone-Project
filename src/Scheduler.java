/**
 * Class Scheduler models a scheduler to receive fire events and dispatch drones
 * @version 1.0
 * @author Nick Fuda
 */

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Scheduler implements Runnable {
    private FireEvent current;
    private boolean shutdownFIS;
    private boolean shutdownDrones;
    private Simulation simulation;
    private ArrayList<FireEvent> eventList = new ArrayList<>();
    private DatagramSocket clientSocket, serverSocket;
    private InetAddress serverAddress;
    private int serverPort = 6000;
    private InetAddress lastClientAddress;
    private int lastClientPort;
    private Thread clientToServerThread, serverToClientThread;

    /**
     * Constructor for scheduler class
     */
    public Scheduler(Simulation simulation){
        eventList = new ArrayList<>();
        current = null;
        shutdownFIS = false;
        shutdownDrones = false;
        this.simulation = simulation;
        try {
            clientSocket = new DatagramSocket(5000);
            serverSocket = new DatagramSocket(5001);
            serverAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void startRPC() {
        System.out.println(
                "Host started on port " + clientSocket.getLocalPort() + " and " + serverSocket.getLocalPort() + "\n");
        clientToServerThread = new Thread(new ClientToServer());
        serverToClientThread = new Thread(new ServerToClient());

        clientToServerThread.start();
        serverToClientThread.start();
    }

    private class ClientToServer implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    // Client packet gets received
                    byte[] receiveData = new byte[100];
                    DatagramPacket clientPacket = new DatagramPacket(receiveData, receiveData.length);
                    clientSocket.receive(clientPacket);

                    // get request from client
                    String message = new String(clientPacket.getData(), 0, clientPacket.getLength());
                    System.out.println("[Host] Got from client: " + message + " (from " + clientPacket.getAddress()
                            + ":" + clientPacket.getPort() + ")");

                    // Save the clients address (localhost in our case) and port number for sending
                    // the packet back
                    synchronized (Scheduler.this) {
                        lastClientAddress = clientPacket.getAddress();
                        lastClientPort = clientPacket.getPort();
                    }

                    // reply to client
                    String acceptMessage = "ACCEPT(" + message + ")";
                    byte[] acceptData = acceptMessage.getBytes();
                    DatagramPacket acceptPacket = new DatagramPacket(acceptData, acceptData.length,
                            clientPacket.getAddress(), clientPacket.getPort());
                    clientSocket.send(acceptPacket);
                    System.out.println("[Host -> Client] Sent immediate ACCEPT to " + clientPacket.getAddress() + ":"
                            + clientPacket.getPort());

                    // forward to server
                    DatagramPacket serverPacket = new DatagramPacket(receiveData, clientPacket.getLength(),
                            serverAddress, serverPort);
                    serverSocket.send(serverPacket);
                    System.out.println("[Host -> Server] Forwarded request to server: " + message + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private class ServerToClient implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    // Server packet gets received
                    byte[] receiveData = new byte[100];
                    DatagramPacket serverPacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(serverPacket);

                    String message = new String(serverPacket.getData(), 0, serverPacket.getLength());
                    System.out.println("[Host] Got from server: " + message + " (from " + serverPacket.getAddress()
                            + ":" + serverPacket.getPort() + ")");

                    // send response back to the last known client
                    synchronized (Scheduler.this) {
                        DatagramPacket clientPacket = new DatagramPacket(receiveData, serverPacket.getLength(),
                                lastClientAddress, lastClientPort);
                        clientSocket.send(clientPacket);
                        System.out.println("[Host -> Client] Forwarded server response to " + lastClientAddress + ":"
                                + lastClientPort + ": " + message + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
     * Adds an event to the scheduler
     * @param event the event to be added
     */
    public synchronized void addEvent(FireEvent event){
        while(current != null) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        //puts HIGH severity FireEvents at the front of the queue, and the rest at the back
        System.out.println("Event added: " + event);
        if(event.getSeverity().equals("High")){
            eventList.addFirst(event);
        } else {
            eventList.addLast(event);
        }
        current = eventList.getFirst();
        notifyAll();
    }

    /**
     * Get the event stored
     * @return the event stored
     */
    public synchronized FireEvent getEvent(){
        while (eventList.isEmpty() && !shutdownDrones) {
            try {
                System.out.println("Drone waiting for an event...");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        if (!eventList.isEmpty()) {
            FireEvent nextEvent = eventList.removeFirst();
            System.out.println("Drone received event: " + nextEvent);
            return nextEvent;
        }

        return null;
    }

    /**
     * Allows drone thread to shut down from waiting for getEvent()
     */
    public synchronized void shutdown(){
        shutdownDrones = true;
        simulation.endSimulation();
        notifyAll();

    }


    /**
     * Notify all processes that an event was handled, clearing the scheduler for a new event
     * @param event the event that will be reported as complete
     */
    public synchronized void notifyCompletion(FireEvent event){
        System.out.println("Event resolved: " + event);
        if(!eventList.isEmpty()){
            current = eventList.getFirst();
        } else {
            current = null;
        }

        notifyAll();
    }

    @Override
    public void run() {
        while(!shutdownFIS && !Thread.currentThread().isInterrupted()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        //prepare to shutdown after last data read from csv
        shutdown();
        System.out.println("Shutting down Scheduler");
    }

    public static void main(String[] args) {
        Simulation simulation = new Simulation(1);
        Scheduler scheduler = new Scheduler(simulation);
        scheduler.startRPC();
    }

}
