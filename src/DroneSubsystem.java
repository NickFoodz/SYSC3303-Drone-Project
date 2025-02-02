/**
 * Class Drone Subsystem models a drone consulting the scheduler to check for events
 * @version 1.0
 * @author Nick Fuda
 */
public class DroneSubsystem implements Runnable {
    private String name;
    private final Scheduler scheduler;

    /**
     * Constructor for the Drone Subsystem
     *
     * @param name      name of the drone
     * @param scheduler scheduler
     */
    public DroneSubsystem(String name, Scheduler scheduler) {
        this.name = name;
        this.scheduler = scheduler;
    }

    public void fightFire() {
        FireEvent current = scheduler.getEvent();
        try {
            if (current != null) {
                System.out.println(name + " assigned to event: " + current);
                Thread.sleep(500); // models time to execute activity
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
}


