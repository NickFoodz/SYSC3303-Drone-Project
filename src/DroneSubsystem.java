import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

/**
 * Class Drone Subsystem models a drone consulting the scheduler to check for events
 * @version 1.0
 * @author Nick Fuda
 */
public class DroneSubsystem implements Runnable {
    private String name;
    private final Scheduler scheduler;
    private final ArrayList<Drone> droneList;
    private enum droneState{
        IDLE, //Drone is not performing any actions
        ENROUTE, //Drone is approaching an incident
        DEPLOYINGAGENT, //Drone is deploying firefighting agent
        RETURNING; //Drone is returning to base
    }
    //iter3
    private DatagramPacket receivePacket, responsePacket;
    private DatagramSocket sendReceiveSocket;
    private InetAddress hostAddress;
    private int hostPortNum;

    /**
     * Constructor for the Drone Subsystem
     * @param name      name of the drone
     * @param scheduler scheduler
     */
    public DroneSubsystem(String name, Scheduler scheduler) {
        this.name = name;
        this.scheduler = scheduler;
        droneState state = droneState.IDLE; //Starting state is idle
        droneList = new ArrayList<>();
        initializeDrones();
    }

    public DroneSubsystem(String name) {
        this.name = name;
        this.scheduler = null;
        droneState state = droneState.IDLE; //Starting state is idle
        droneList = new ArrayList<>();
        initializeDrones();

        //iter3
        try {
            sendReceiveSocket = new DatagramSocket(6000);
            hostAddress = InetAddress.getLocalHost();
            hostPortNum = 5001;
        } catch (SocketException | UnknownHostException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }


    public void rpc_send(DatagramPacket dataToSend, DatagramPacket dataReceived) {
        try {
            // Server is waiting here...

            sendReceiveSocket.receive(dataReceived);

            String request = new String(dataReceived.getData(), 0, dataReceived.getLength());
            System.out.println("[Server] Received: " + request + " from Host(" + dataReceived.getAddress() + ":"
                    + dataReceived.getPort() + ")");

            String response = request + " handled!";

            dataToSend.setData(response.getBytes());
            sendReceiveSocket.send(dataToSend);
            System.out.println("[Server] Sent response back to Host: " + response);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startRPC() {
        while (true) {
            byte receiveData[] = new byte[100];
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            byte responseData[] = new byte[100];
            responsePacket = new DatagramPacket(responseData, responseData.length, hostAddress, hostPortNum);

            rpc_send(responsePacket, receivePacket);
        }
    }


    /**
     * Initializes subsystem to have 3 drones
     * assume only 1 for iteration 2
     */
    public void initializeDrones(){
        Drone drone1 = new Drone("Drone 1");
        //Drone drone2 = new Drone("Drone 2");
        //Drone drone3 = new Drone("Drone 3");
        droneList.add(drone1);
        //droneList.add(drone2);
        //droneList.add(drone3);
    }

    public int putOutFire(FireEvent event){
        String sev = event.getSeverity();
        int waterToUse = 0;
        switch(sev){
            case("High"):
                waterToUse = 30;
                break;
            case("Moderate"):
                waterToUse = 20;
                break;
            case("Low"):
                waterToUse = 10;
                break;
        }
        return waterToUse;
    }

    public void fightFire() {
        FireEvent current = scheduler.getEvent();
        try {
            if (current != null) {
                int water = putOutFire(current);
                System.out.println(name + " assigned to event at Zone " + current.getZoneID() + ". En route to location");
                System.out.println(name + " arrived at Zone " + current.getZoneID() + ", resolving event " + current.getType());
                Thread.sleep(500); // models time to execute activity
                System.out.println("Using " + water + "L of water to put out fire");
                scheduler.notifyCompletion(current);
            } else {
                Thread.sleep(500); //Waits to try again
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            //*FOR JUNIT TESTS*
            System.out.println("DroneSubsystem thread interrupted, shutting down.");
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }


    @Override
    public void run() {
        while (!scheduler.getShutdownDrones()) {
            fightFire();
        }
        System.out.println("Shutting down Drone Subsystem");
    }

    public static void main(String[] args) {
        DroneSubsystem ds = new DroneSubsystem("Subsystem 1");
        ds.startRPC();
    }


    /**
     * Helper Class Drone represents the drones that exist in the Drone Subsystem
     */
    private class Drone{
        private String DroneID;
        private droneState state;
        private int x, y; //coordinates of the drone's location

        /**
         * Constructor for drones
         * @param ID the name of the drone
         */
        public Drone(String ID){
            DroneID = ID;
            state = droneState.IDLE;
        }

        /**
         * Get the name of the drone
         * @return String ID of the drone
         */
        public String getDroneID() {return DroneID;}

        /**
         * Get the state of the Drone
         * @return droneState state of drone
         */
        public droneState getState(){return state;}

        /**
         * Get x coordinate of the drone
         * @return x coordinate of the drone
         */
        public int getDroneXLocation(){return x;}

        /**
         * Get the y coordinate of the drone
         * @return y coordinate of the drone
         */
        public int getDroneYLocation(){return y;}
    }
}



