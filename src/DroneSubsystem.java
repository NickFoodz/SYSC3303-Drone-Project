
import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.net.*;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO:
 * if another task comes into a drone and is closer, the drone will handle that task first.
 *      -> will need to put a queue in each drone for this. Will probably require a scheduler redo :(
 * GUI!!!
 */


/**
 * Class Drone Subsystem models a drone consulting the scheduler to check for events
 * @version 2.0 - Added state machine support
 * @author Nick Fuda
 */
public class DroneSubsystem {
    private String name;
    private String zoneFilePath = "Sample_zone_file.csv"; //File path to the .csv
    private final int schedulerPort = 6001;
    private final ArrayList<Drone> droneList;
    FireEvent current;
    private int DroneSubsystemPort = 5000;
    private int DroneSubsystemSendPort = 5020;
    private int index;
    private DatagramSocket subsystemSocket, droneSendSocket;
    private int numberOfDrones = 10;
    private List<Zone> allZones;
    private GUI gui;


    /**
     * Constructor for the Drone Subsystem
     *
     * @param name name of the drone subsystem
     */
    public DroneSubsystem(String name) {
        this.name = name;

        //droneState state = droneState.IDLE; //Starting state is idle
        droneList = new ArrayList<>();
        current = null;
        index = 0;
        allZones = new ArrayList<>();
        try {
            subsystemSocket = new DatagramSocket(DroneSubsystemPort);
            droneSendSocket = new DatagramSocket(DroneSubsystemSendPort);
        } catch (SocketException se) {
            se.printStackTrace();
        }

        readZones();

        gui = new GUI();
        gui.updateDimensions(allZones);
        SwingUtilities.invokeLater(() -> {
            gui.setVisible(true);
            gui.addCoords(allZones);
        });
    }

    public void readZones(){
        //Try to read the csv
        try (BufferedReader reader = new BufferedReader(new FileReader(zoneFilePath))) {
            String line;
            reader.readLine(); //First line is header

            //While there is data on the lines, create a new fire event and add it to the scheduler
            while ((line = reader.readLine()) != null) {
                String[] info = line.split(",");
                if (info.length == 3) {
                    int zoneID = Integer.parseInt(info[0]);
                    String[] zoneStart = info[1].replaceAll("[()]", "").split(";");;
                    String[] zoneEnd = info[2].replaceAll("[()]", "").split(";");;

                    Zone zone = new Zone(zoneID, Integer.parseInt(zoneStart[0]), Integer.parseInt(zoneStart[1]), Integer.parseInt(zoneEnd[0]), Integer.parseInt(zoneEnd[1]));

                    allZones.add(zone);
                }
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Zone getZone(int id){
        for (Zone z : allZones){
            if(z.getZoneId() == id){
                return z;
            }
        }
        //should never reach if input files are correct
        return null;
    }

    public void TESTING_closeSockets(){
        subsystemSocket.close();
        droneSendSocket.close();
        for(int i = 0; i < droneList.size(); i++){
            droneList.get(i).TESTING_closeSocket();
        }
    }

    /**
     * Initializes Drones and adds them to the list that is maintained by the subsystem
     */
    public void initializeDrones() {
        for (int i = 0; i < numberOfDrones; i++){
            //Create x drones
            String id = "Drone " + (i+1);
            Drone drone = new Drone(id, i+1, this, (5000 + (i+1)));
            //Add drones to the List (drones at the base, idle)
            droneList.add(drone);
            gui.updateDrone(id, 0, 0);

            //Start the threads
            Thread d = new Thread(drone);
            d.start();
        }
    }

    /**
     * Main running method, which receives a packet, converts it into an event and assigns a drone
     */
    public void manageDrones() {
        while (true) {
            //Create buffer and receiving packet
            byte[] receiveData = new byte[100];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                //Wait for message
                subsystemSocket.receive(receivePacket);
                String receiveString = new String(receivePacket.getData(), 0, receivePacket.getLength());

                try {
                    //Convert into FireEvent
                    FireEvent newEvent;
                    String[] info = receiveString.split(",");
                    if (info.length == 6) {
                        String time = info[0];
                        int zoneID = Integer.parseInt(info[1]);
                        String type = info[2];
                        String severity = info[3];
                        String fault = info[4];
                        Zone zone = getZone(zoneID);
                        newEvent = new FireEvent(time, zoneID, type, severity, fault, zone);
                        System.out.println("\nDrone Subsystem received event from Scheduler: " + newEvent + "\n");
                        if (newEvent.getFault().equals("Packet Loss/Corrupted Messages")) {
                            throw new DroneNetworkFailure("Bad Event message..");
                        }
                        assignDrone(receiveString);
                    }


                }  catch (DroneNetworkFailure d){
                    System.out.println("Bad Event Message... sending back not accepting");
                    //RE-ENTER THE EVENT TO BE PROCESSED
                    byte[] denyMsg = new byte[1000];
                    String accept = "REJECT";
                    denyMsg = accept.getBytes();
                    //can change InetAddress to be the host of the scheduler if different
                    DatagramPacket denyPacket = new DatagramPacket(denyMsg, 6, InetAddress.getLocalHost(), 6002);
                    //Send to the scheduler that the task is accepted
                    subsystemSocket.send(denyPacket);
                }
            } catch (IOException | InterruptedException e) {

            }
        }
    }


//    /**
//     * Assign a drone an event given by the scheduler, and remove from the list of available drones
//     *
//     * @throws InterruptedException
//     */
//    public void assignDrone(FireEvent newEvent) throws InterruptedException {
//        droneList.get(index).startEvent(newEvent);
//        droneList.remove(index);
//        index++;
//        //change to 2 if 3 drones
//        //Index is meant to cycle which drones get assigned which tasks
//        if (index % 2 == 0) {
//            index = 0;
//        }
//    }

    /**
     * Assign a drone an event given by the scheduler, and remove from the list of available drones
     *
     * @throws InterruptedException
     */
    public void assignDrone(String eventInfo) throws InterruptedException {
        for (Drone drone : droneList) {
            if (drone.state == Drone.droneState.IDLE) {
                int port = 5000 + drone.droneNum;
                droneList.remove(drone);
                try {
                    byte[] buffer = eventInfo.getBytes();
                    DatagramPacket fwdPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), port);
                    droneSendSocket.send(fwdPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        System.out.println("Cant assign drone");
    }

    /**
     * Returns the list of drones
     *
     * @return the drone list
     */
    public ArrayList<Drone> getDroneList() {
        return droneList;
    }

    /**
     * For use by drones when they complete their event handling
     *
     * @param drone
     */
    public void returnDroneToList(Drone drone) {
        //When a drone returns it adds itself back to the list
        droneList.add(drone);
    }

    public void removeDroneFromList(Drone drone) {
        //When a drone returns it adds itself back to the list
        droneList.remove(drone);
    }

    /**
     * Main method of the program
     *
     * @param args
     */
    public static void main(String[] args) {
        DroneSubsystem droneSub = new DroneSubsystem("Drone Subsystem");
        droneSub.initializeDrones();
        System.out.println("Drone Subsystem Online");
        droneSub.manageDrones();
    }


    /**
     * Helper Class Drone represents the drones that exist in the Drone Subsystem
     */
    public class Drone implements Runnable {
        private String DroneID;

        public enum droneState {
            IDLE {
                @Override
                public String toString() {
                    return "idle";
                }
            }, //Drone is not performing any actions
            ENROUTE {
                @Override
                public String toString() {
                    return "en route";
                }
            }, //Drone is approaching an incident
            DEPLOYINGAGENT {
                @Override
                public String toString() {
                    return "deploying agent";
                }
            }, //Drone is deploying firefighting agent
            RETURNING {
                @Override
                public String toString() {
                    return "returning to base";
                }
            }, //Drone is returning to base
            DISABLED {
                @Override
                public String toString() {
                    return "drone is disabled";
                }; //Drone is disabled
            }
        }

        private int droneNum;
        private droneState state;
        private int x, y; //coordinates of the drone's location
        private int destX, destY; // destination of drone's location
        private final int speed = 60; // km/h
        private FireEvent currentEvent;
        private double travelTime;
        private double slope;
        private double y_intercept;
        private boolean dir; // for calculating coordinates: true is positive, false is negative

        //agent tank
        private int tank;
        private final int TANK_MAX = 15; // 15L max

        //Socket to send updates to the Scheduler
        private DatagramSocket droneSocket;
        private ArrayList<String> log;

        //fireEvent Queue
        private Deque<FireEvent> eventQueue;

        /**
         * Constructor for drones
         *
         * @param ID the name of the drone
         */
        public Drone(String ID, int droneNum, DroneSubsystem ParentSystem, int socketNumber) {
            DroneID = ID;
            this.droneNum = droneNum;
            state = droneState.IDLE;
            travelTime = 0.0;
            tank = TANK_MAX;
            log = new ArrayList<>();
            log.add("IDLE");

            eventQueue = new LinkedList<>();
            try {
                droneSocket = new DatagramSocket(socketNumber);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        public void TESTING_closeSocket(){
            this.droneSocket.close();
        }

        /**
         * Sends the status of the drone to the scheduler (not fully implemented, having issues with multithreading
         */
        public void sendStatus() {
            //Set status
            String status = new String("State of " + DroneID + ": " + this.state.toString());
//            String status = "Event complete: " + currentEvent.toString();
            try {
                //Set up UDP
                byte[] statusData = new byte[100];
                statusData = status.getBytes();
                //Can change InetAddress if different machines
                DatagramPacket sendStatus = new DatagramPacket(statusData, status.length(), InetAddress.getLocalHost(), schedulerPort); //6001
                this.droneSocket.send(sendStatus);
            } catch (IOException e) {
            }

        }

        /**
         * Drone is idle initially and when awaiting a task
         */
        private void idle() {
            sendStatus();
            while (true) {
                FireEvent newEvent = waitForSignal();
                if(newEvent != null){
                    eventQueue.addLast(newEvent);
                }
                if(!eventQueue.isEmpty() && state == droneState.IDLE){
                    try{
                        startEvent(eventQueue.poll());
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted");
                    }
                }
            }
        }

        /**
         * Drone accepts a task from the scheduler when able and signals it is accepted
         * Allowing the scheduler to move on and send the next task to the subsystem
         */
        private void acceptTask() {
            try {
                //Buffer
                byte[] acceptancemsg = new byte[100];
                String accept = "ACCEPT";
                acceptancemsg = accept.getBytes();
                //can change InetAddress to be the host of the scheduler if different
                DatagramPacket acceptPacket = new DatagramPacket(acceptancemsg, 6, InetAddress.getLocalHost(), 6002);
                //Send to the scheduler that the task is accepted
                this.droneSocket.send(acceptPacket);
            } catch (IOException e) {
            }
        }

        /**
         * Drone is dispatched to event and performs firefighting
         *
         * @param e the event
         * @throws InterruptedException
         */
        public void startEvent(FireEvent e) throws InterruptedException {
            acceptTask();
            this.currentEvent = e;
            this.fightFire();
        }

        /**
         * Calculates how much water is needed to put out the fire based on the severity
         *
         * @param event the event with the fire
         * @return the amount of water in L
         */
        public int putOutFire(FireEvent event) {
            int needed = event.getNeededToPutOut();

            int waterUsed = Math.min(needed, tank);
            event.setNeededToPutOut(needed - waterUsed);
            tank -= waterUsed;

            return waterUsed;
        }

        public FireEvent waitForSignal(){
            byte[] eventData = new byte[100];
            DatagramPacket eventPacket = new DatagramPacket(eventData, eventData.length);
            try {
//                droneSocket.setSoTimeout(100);
                this.droneSocket.receive(eventPacket);
                String receiveString = new String(eventPacket.getData(), 0, eventPacket.getLength());

                //Convert into FireEvent
                FireEvent newEvent;
                String[] info = receiveString.split(",");
                if (info.length == 6) {
                    String time = info[0];
                    int zoneID = Integer.parseInt(info[1]);
                    String type = info[2];
                    String severity = info[3];
                    String fault = info[4];
                    Zone zone = getZone(zoneID);
                    int agentNeeded = Integer.parseInt(info[5]);
                    newEvent = new FireEvent(time, zoneID, type, severity, fault, zone, agentNeeded);
                    System.out.println(newEvent);
                    return newEvent;
                }

            }catch(IOException e){}
            return null;
        }

        /**
         * Method that runs the drone through the state machine
         *
         * @throws InterruptedException
         */
        private void fightFire() throws InterruptedException {
            enRoute(); //State 2 from idle, carries into next states, beginning the state machine
            returnDroneToList(this);
        }

        /**
         * Method that happens when the drone is in the ENROUTE state, moves to DEPLOYINGAGENT at end
         *
         * @throws InterruptedException
         */
        private void enRoute() throws InterruptedException {
            try {
                int[] dest = currentEvent.getZone().calculateCenter();
                destX = dest[0];
                destY = dest[1];

                state = droneState.ENROUTE; //Changes state
                log.add("ENROUTE");
                sendStatus();
                System.out.println(DroneID + " is en route to Zone " + currentEvent.getZoneID());
                travelTime = methodToCalculateTravelTime();
                while (x != destX && y != destY) {
                    int[] coords = calculateNewCoordinates();
                    x = coords[0];
                    y = coords[1];
                    gui.updateDrone(DroneID, x, y);
                    Thread.sleep(500);
                }

                if (currentEvent.getFault().equals("Drone Stuck")) {
                    throw new DroneStuck(DroneID + " is stuck. Returning and reassigning event");
                }

                //Go to next state
                deployAgent();
            } catch (DroneStuck d){
                System.out.println(DroneID + " is stuck. Returning and reassigning event");
                //SEND BACK
                returnToBase();

                //RE-ENTER THE EVENT TO BE PROCESSED
                currentEvent.clearFault();
                sendEventToDroneSubsystem(currentEvent);
            }
        }

        private double methodToCalculateTravelTime() {
            double dist = Math.sqrt(Math.pow((destY - y),2) + Math.pow((destX - x), 2));
            slope = (double) (destY - y) / (destX - x);
            y_intercept = y - (slope * x);
            if (slope >= 0) {
                if (destY > y && destX > x) {
                    dir = true;
                }
                else {
                    dir = false;
                }
            }
            else {
                if (destY < y && destX > x) {
                    dir = true;
                }
                else {
                    dir = false;
                }
            }
            return Math.ceil(dist / speed);
        }

        private int[] calculateNewCoordinates() {
            // check for overshooting
            if (Math.sqrt(Math.pow((destY - y),2) + Math.pow((destX - x), 2)) >= speed) {
                return new int[]{destX, destY};
            }

            double coordX = speed / Math.sqrt(1 + Math.pow(slope, 2));
            double coordY = slope * speed / Math.sqrt(1 + Math.pow(slope, 2));
            int[] coords = new int[2];
            if (dir) { // check direction of drone
                coords[0] = (int) (x + coordX);
                coords[1] = (int) (y + coordY);
            }
            else {
                coords[0] = (int) (x - coordX);
                coords[1] = (int) (y - coordY);
            }
            return coords;
        }

        private boolean passZone(Zone z) {
            if (state.equals(droneState.ENROUTE)) {
                // y = mx + b
                double leftY = (slope * z.getStartX()) + y_intercept;
                double rightY = (slope * z.getEndX()) + y_intercept;
                double topX = (z.getStartY() - y_intercept) / slope;
                double bottomX = (z.getEndY() - y_intercept) / slope;

                // check if it is within path and intersects the zone
                if ((leftY >= Math.min(x, destX) && leftY <= Math.max(x, destX) && leftY >= z.getStartX() && leftY <= z.getEndX()) ||
                    (rightY >= Math.min(x, destX) && rightY <= Math.max(x, destX) && rightY >= z.getStartX() && rightY <= z.getEndX()) ||
                    (topX >= Math.min(y, destY) && topX <= Math.max(y, destY) && topX >= z.getStartY() && topX <= z.getEndY()) ||
                    (bottomX >= Math.min(y, destY) && bottomX <= Math.max(y, destY) && bottomX >= z.getStartY() && bottomX <= z.getEndY())) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Method that happens when the drone is in the DEPLOYINGAGENT state, moves to RETURNING state at end
         *
         * @throws InterruptedException
         */
        private void deployAgent() throws InterruptedException {
            try {
                state = droneState.DEPLOYINGAGENT; //Change state
                log.add("DEPLOYINGAGENT");
                sendStatus();
                System.out.println(DroneID + " arrived at Zone " + currentEvent.getZoneID() + ", attempting to deploy agent");

                if (currentEvent.getFault().equals("Nozzle Jammed")) {
                    throw new DroneNozzleStuck(DroneID + "'s Nozzle is stuck. Disabling");
                }

                int agentUsed = putOutFire(currentEvent);
                System.out.println(DroneID + " at Zone " + currentEvent.getZoneID() + ", deployed " + agentUsed + "L of agent");

                Thread.sleep((agentUsed / 10) * 1000L);
                //Go to next state
                if(currentEvent.getNeededToPutOut() != 0){
                    System.out.println(DroneID + " Should be handling this event next " + currentEvent);
//                    eventQueue.addFirst(currentEvent); //legacy; keep for now
                    sendEventToDroneSubsystem(currentEvent);

                }
                returnToBase();

            } catch (DroneNozzleStuck d){
                System.out.println(DroneID + "'s Nozzle is stuck. Sending back to base and disabling.");

                //SEND BACK
                returnToBase();

                //DISABLE THE DRONE
                state = droneState.DISABLED;
                log.add("DISABLED");
                sendStatus();
                //for(String n : log){
                   // System.out.println(n);
                //}

                //RE-ENTER THE EVENT TO BE PROCESSED
                currentEvent.clearFault();
                sendEventToDroneSubsystem(currentEvent);

            }
        }

        private void sendEventToDroneSubsystem(FireEvent currentEvent){
            byte[] outData = new byte[100];
            String eventString = currentEvent.summarizeEvent();
            outData = eventString.getBytes();
            try {
                DatagramPacket sendEvent = new DatagramPacket(outData, eventString.length(), InetAddress.getLocalHost(), 5000);
                //Try sending to Drone subsystem
                droneSocket.send(sendEvent);
            } catch (IOException e) {

            }
        }

        /**
         * Method that happens when the drone is in the RETURNING state, returns the drone to the IDLE state
         */
        private synchronized void returnToBase() throws InterruptedException {
            state = droneState.RETURNING;
            log.add("RETURNING");
            sendStatus();
            System.out.println(DroneID + " returning to base");

            destX = 0;
            destY = 0;
            while (x != destX && y != destY) {
                int[] coords = calculateNewCoordinates();
                x = coords[0];
                y = coords[1];
                gui.updateDrone(DroneID, x, y);
                Thread.sleep(500);
            }

            travelTime = 0;
            //Return to the first state
            tank = TANK_MAX;
            state = droneState.IDLE;
            log.add("IDLE");
            //sendStatus();
        }

        /**
         * Get the name of the drone
         *
         * @return String ID of the drone
         */
        public String getDroneID() {
            return DroneID;
        }

        /**
         * Get the state of the Drone
         *
         * @return droneState state of drone
         */
        public droneState getState() {
            return state;
        }

        /**
         * Get x coordinate of the drone
         *
         * @return x coordinate of the drone
         */
        public int getDroneXLocation() {
            return x;
        }

        /**
         * Get the y coordinate of the drone
         *
         * @return y coordinate of the drone
         */
        public int getDroneYLocation() {
            return y;
        }

        /**
         * Get the state logs for debugging
         *
         * @return array list of log containing states the drone went through
         */
        public ArrayList<String> getLog() {
            return log;
        }

        @Override
        public void run() {
            System.out.println(DroneID + " online");
            idle();
        }
    }
    public class DroneStuck extends Exception {
        public DroneStuck(String message) {
            super(message);
        }
    }
    public class DroneNozzleStuck extends Exception {
        public DroneNozzleStuck(String message) {
            super(message);
        }
    }
    public class DroneNetworkFailure extends Exception {
        public DroneNetworkFailure(String message) {
            super(message);
        }
    }
}
