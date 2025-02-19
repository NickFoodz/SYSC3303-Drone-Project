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
        ENROUTE,
        DEPLOYINGAGENT,
        RETURNING;
    }

    /**
     * Constructor for the Drone Subsystem
     *
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

    /**
     * Initializes subsystem to have 3 drones
     */
    public void initializeDrones(){
        Drone drone1 = new Drone("Drone 1");
        Drone drone2 = new Drone("Drone 2");
        Drone drone3 = new Drone("Drone 3");
        droneList.add(drone1);
        droneList.add(drone2);
        droneList.add(drone3);
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
            throw new RuntimeException(e);
        }
    }


    @Override
    public void run() {
        while (!scheduler.getShutdownDrones()) {
            fightFire();
        }
        System.out.println("Shutting down Drone Subsystem");
    }


    /**
     * Helper Class Drone represents the drones that exist in the Drone Subsystem
     */
    private class Drone{
        private String DroneID;
        private droneState state;
        private int x, y;

        public Drone(String ID){
            DroneID = ID;
            state = droneState.IDLE;
        }

        public String getDroneID() {
            return DroneID;
        }

        public droneState getState(){
            return state;
        }
    }
}



