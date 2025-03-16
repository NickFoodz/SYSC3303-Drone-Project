import java.time.LocalTime;

/**
 * Simulation Class, simulates time incrementing for event handling
 * @version 1.0
 * @author Thomas Imbert
 */
public class Simulation implements Runnable {
    private int simulationSpeed;                            // Simulation Speed in ms/s (I have it set to 1 in a lot of places for testing)
    private int time = 46200;                               // Global what time it is in the sim (starting at like 12:50, will have to revise, maybe make it a localtime object)
    private boolean running;                                // If the simulation is running or not
    private FireIncidentSubsystem incidentSubsystem;        //FIS

    public Simulation(int simSpeed) {
        this.simulationSpeed = simSpeed;
        running = true;
    }

    /**
     * Overrides the run function in Runnable
     * While the simulation is running, check if theres an event that needs to be handled at each current time
     * Increments the simulation time
     */
    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(simulationSpeed);
            } catch (InterruptedException e) {
                System.out.println("simulation thread interrupted, shutting down.");
                Thread.currentThread().interrupt();
            }

            LocalTime currentSimTime = LocalTime.of(getHours(), getMinutes(), getSeconds());
//            System.out.println(currentSimTime); //testing

            // check with FireIncidentSubsystem if an event should occur now
            //incidentSubsystem.consumeEvent(currentSimTime);

            time++; // Increment simulation time
        }
    }

    /**
     * Setter to end the simulation
     */
    public void endSimulation(){
        running = false;
    }

    /**
     * Setter for the FireIncidentSubsystem
     */
    public void setFireIncidentSubsystem(FireIncidentSubsystem f){
        incidentSubsystem = f;
    }

    /**
     * Getter for the time in hours
     * Functions to create LocalTime object.
     */
    private int getHours() {
        return time / 3600;
    }
    /**
     * Getter for the time in minutes
     * Functions to create LocalTime object.
     */
    private int getMinutes() {
        return (time - (getHours() * 3600)) / 60;
    }
    /**
     * Getter for the time in seconds
     * Functions to create LocalTime object.
     */
    private int getSeconds() {
        return time - (getHours() * 3600) - (getMinutes() * 60);
    }

}