import java.time.LocalTime;

/**
 * Simulation Class, simulates time incrementing for event handling
 * @version 1.0
 * @author Thomas Imbert
 */
public class Simulation implements Runnable {
    private int simulationSpeed;                        // Simulation Speed in ms/s (I have it set to 1 in a lot of places for testing)
    private int time = 46200;                           // Global what time it is in the sim (starting at like 12:50, will have to revise, maybe make it a localtime object)
    private boolean running;                            // If the simulation is running or not
    private FireIncidentSubsystem incidentSubsystem;    //FIS

    public Simulation(int simSpeed) {
        this.simulationSpeed = simSpeed;
        running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(simulationSpeed);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            LocalTime currentSimTime = LocalTime.of(getHours(), getMinutes(), getSeconds());
//            System.out.println(currentSimTime); //testing

            // check with FireIncidentSubsystem if an event should occur now
            incidentSubsystem.consumeEvent(currentSimTime);

            time++; // Increment simulation time
        }
    }

    public void endSimulation(){
        running = false;
    }

    public void setFireIncidentSubsystem(FireIncidentSubsystem f){
        incidentSubsystem = f;
    }

    //Functions to create LocalTime object.
    private int getHours() {
        return time / 3600;
    }
    private int getMinutes() {
        return (time - (getHours() * 3600)) / 60;
    }
    private int getSeconds() {
        return time - (getHours() * 3600) - (getMinutes() * 60);
    }

}