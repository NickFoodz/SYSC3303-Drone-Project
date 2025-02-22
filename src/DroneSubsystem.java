import java.util.ArrayList;

/**
 * Class Drone Subsystem models a drone consulting the scheduler to check for events
 * @version 2.0 - Added state machine support
 * @author Nick Fuda
 */
public class DroneSubsystem implements Runnable {
    private String name;
    private final Scheduler scheduler;
    private final ArrayList<Drone> droneList;
    FireEvent current;
    private int index;


    /**
     * Constructor for the Drone Subsystem
     * @param name      name of the drone
     * @param scheduler scheduler
     */
    public DroneSubsystem(String name, Scheduler scheduler) {
        this.name = name;
        this.scheduler = scheduler;
        droneList = new ArrayList<>();
        current = null;
        index = 0;
        initializeDrones();
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
        //Commented out drones are for when more drones are necessary
    }


    /**
     * Assign a drone an event given by the scheduler, and remove from the list of available drones
     * @throws InterruptedException
     */
    public void assignDrone() throws InterruptedException {
        droneList.get(index).startEvent(current);
        droneList.remove(index);
        index ++;
        //change to 2 if 3 drones
        //Index is meant to cycle which drones get assigned which tasks
        if(index % 1 == 0){
            index = 0;
        }
    }

    /**
     * For use by drones when they complete their event handling
     * @param drone
     */
    public void returnDroneToList(Drone drone){
        //When a drone returns it adds itself back to the list
        droneList.add(drone);
    }

    /**
     * Receives tasks from scheduler and sends drones to deal with them
     * @throws InterruptedException
     * @throws RuntimeException
     */
    public void fightFire() throws InterruptedException, RuntimeException {
        current = scheduler.getEvent();
        //If no drones available, wait
        while(droneList.isEmpty()){
            System.out.println("Waiting for drone to return");
            try{
            wait();} catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        //Otherwise, assign a drone to the event or wait if the scheduler has no task
        try {
            if (current != null) {
                assignDrone();
            } else {
                Thread.sleep(500); //Waits to try again
            }
        } catch (RuntimeException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void run() {
        while (!scheduler.getShutdownDrones()) {
            try {
                fightFire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Shutting down Drone Subsystem");
    }


    /**
     * Helper Class Drone represents the drones that exist in the Drone Subsystem
     */
    private class Drone{
        private String DroneID;
        private enum droneState{
            IDLE, //Drone is not performing any actions
            ENROUTE, //Drone is approaching an incident
            DEPLOYINGAGENT, //Drone is deploying firefighting agent
            RETURNING; //Drone is returning to base
        }
        private droneState state;
        private int x, y; //coordinates of the drone's location
        private final int speed = 60; // km/h
        private FireEvent currentEvent;
        private double travelTime;

        /**
         * Constructor for drones
         * @param ID the name of the drone
         */
        public Drone(String ID){
            DroneID = ID;
            state = droneState.IDLE;
            travelTime = 0.0;
        }

        /**
         * Drone is dispatched to event and performs firefighting
         * @param e the event
         * @throws InterruptedException
         */
        public void startEvent(FireEvent e) throws InterruptedException {
            this.currentEvent = e;
            this.fightFire();
        }

        /**
         * Calculates how much water is needed to put out the fire based on the severity
         * @param event the event with the fire
         * @return the amount of water in L
         */
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

        /**
         * Method that runs the drone through the state machine
         * @throws InterruptedException
         */
        private void fightFire() throws InterruptedException {
            enRoute(); //State 2 from idle, carries into next states, beginning the state machine
            scheduler.notifyCompletion(currentEvent);
            returnDroneToList(this);
        }

        /**
         * Method that happens when the drone is in the ENROUTE state, moves to DEPLOYINGAGENT at end
         * @throws InterruptedException
         */
        private void enRoute() throws InterruptedException {
            state = droneState.ENROUTE; //Changes state
            System.out.println(DroneID + " is en route to Zone " + currentEvent.getZoneID());
            //travelTime = methodToCalculateTravelTime
            Thread.sleep(500);
            //Go to next state
            deployAgent();
        }

        /**
         * Method that happens when the drone is in the DEPLOYINGAGENT state, moves to RETURNING state at end
         * @throws InterruptedException
         */
        private void deployAgent()throws InterruptedException {
            state = droneState.DEPLOYINGAGENT; //Change state
            System.out.println(DroneID + " arrived at Zone " + currentEvent.getZoneID() +", deploying agent");
            int waterToUse = putOutFire(currentEvent);
            Thread.sleep(500);
            //Go to next state
            returnToBase();
        }

        /**
         * Method that happens when the drone is in the RETURNING state, returns the drone to the IDLE state
         */
        private void returnToBase() throws InterruptedException {
            state = droneState.RETURNING;
            System.out.println(DroneID + " returning to base");
            Thread.sleep(500); //change to travel time
            travelTime = 0;
            //Return to the first state
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



