
import java.io.IOException;
import java.util.ArrayList;
import java.net.*;
/**
 * Class Drone Subsystem models a drone consulting the scheduler to check for events
 * @version 2.0 - Added state machine support
 * @author Nick Fuda
 */
public class DroneSubsystem {
    private String name;
    private final int schedulerPort = 6001;
    private final ArrayList<Drone> droneList;
    FireEvent current;
    private int DroneSubsystemPort = 5000;
    private int DroneSubsystemSendPort = 5004;
    private int index;
    private DatagramSocket subsystemSocket, droneSendSocket;
    private int numberOfDrones = 3;

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
        try {
            subsystemSocket = new DatagramSocket(DroneSubsystemPort);
            droneSendSocket = new DatagramSocket(DroneSubsystemSendPort);
        } catch (SocketException se) {
            se.printStackTrace();
        }
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
            Drone drone = new Drone("Drone " + (i+1), this, (5000 + (i+1)));
            //Add drones to the List (drones at the base, idle)
            droneList.add(drone);
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
                    if (info.length == 5) {
                        String time = info[0];
                        int zoneID = Integer.parseInt(info[1]);
                        String type = info[2];
                        String severity = info[3];
                        String fault = info[4];
                        newEvent = new FireEvent(time, zoneID, type, severity, fault);
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
                int port = 5000 + droneList.indexOf(drone) + 1;
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

        private droneState state;
        private int x, y; //coordinates of the drone's location
        private int destX, destY; // destination of drone's location
        private final int speed = 60; // km/h
        private FireEvent currentEvent;
        private double travelTime;
        private double slope;
        private boolean dir; // for calculating coordinates: true is positive, false is negative

        //Socket to send updates to the Scheduler
        private DatagramSocket droneSocket;
        private ArrayList<String> log;

        /**
         * Constructor for drones
         *
         * @param ID the name of the drone
         */
        public Drone(String ID, DroneSubsystem ParentSystem, int socketNumber) {
            DroneID = ID;
            state = droneState.IDLE;
            travelTime = 0.0;
            log = new ArrayList<>();
            log.add("IDLE");
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
                DatagramPacket sendStatus = new DatagramPacket(statusData, status.length(), InetAddress.getLocalHost(), 6001);
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
                    try {
                        startEvent(newEvent);
                    } catch (InterruptedException e) {}
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
            String sev = event.getSeverity();
            int waterToUse = 0;
            switch (sev) {
                case ("High"):
                    waterToUse = 30;
                    break;
                case ("Moderate"):
                    waterToUse = 20;
                    break;
                case ("Low"):
                    waterToUse = 10;
                    break;
            }
            return waterToUse;
        }

        public FireEvent waitForSignal(){
            byte[] eventData = new byte[100];
            DatagramPacket eventPacket = new DatagramPacket(eventData, eventData.length);
            try {
                this.droneSocket.receive(eventPacket);
                String receiveString = new String(eventPacket.getData(), 0, eventPacket.getLength());

                //Convert into FireEvent
                FireEvent newEvent;
                String[] info = receiveString.split(",");
                if (info.length == 5) {
                    String time = info[0];
                    int zoneID = Integer.parseInt(info[1]);
                    String type = info[2];
                    String severity = info[3];
                    String fault = info[4];
                    newEvent = new FireEvent(time, zoneID, type, severity, fault);
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
                state = droneState.ENROUTE; //Changes state
                log.add("ENROUTE");
                sendStatus();
                System.out.println(DroneID + " is en route to Zone " + currentEvent.getZoneID());
                travelTime = methodToCalculateTravelTime();
                while (x != destX && y != destY) {
                    int[] coords = calculateNewCoordinates();
                    x = coords[0];
                    y = coords[1];
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
        }

        private double methodToCalculateTravelTime() {
            double dist = Math.sqrt(Math.pow((destY - y),2) + Math.pow((destX - x), 2));
            slope = (double) (destY - y) / (destX - x);
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
                System.out.println(DroneID + " arrived at Zone " + currentEvent.getZoneID() + ", deploying " + putOutFire(currentEvent) + "L of agent");

                if (currentEvent.getFault().equals("Nozzle Jammed")) {
                    throw new DroneNozzleStuck(DroneID + "'s Nozzle is stuck. Disabling");
                }

                int waterToUse = putOutFire(currentEvent);
                Thread.sleep(500);
                //Go to next state
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
        }

        /**
         * Method that happens when the drone is in the RETURNING state, returns the drone to the IDLE state
         */
        private synchronized void returnToBase() throws InterruptedException {
            state = droneState.RETURNING;
            log.add("RETURNING");
            sendStatus();
            System.out.println(DroneID + " returning to base");
            Thread.sleep(500); //change to travel time
            travelTime = 0;
            //Return to the first state
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
