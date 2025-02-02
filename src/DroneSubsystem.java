/**
 * Class Drone Subsystem models a drone consulting the scheduler to check for events
 * @version 1.0
 * @author Nick Fuda
 */
public class DroneSubsystem implements Runnable{
    private String name;
    private final Scheduler scheduler;

    /**
     * Constructor for the Drone Subsystem
     * @param name name of the drone
     * @param scheduler scheduler
     */
    public DroneSubsystem(String name, Scheduler scheduler){
        this.name = name;
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        try{
            while(true){
                FireEvent current = scheduler.getEvent();
                if(current != null){
                    System.out.println(name + " assigned to event: " + current);
                    Thread.sleep(500); // models time to execute activity
                    scheduler.notifyCompletion(current);
                }
                else{
                    Thread.sleep(500); //Waits to try again
                }
            }
        } catch (InterruptedException e) {
            System.out.println("drone thread interrupted");
            throw new RuntimeException(e);
        }
    }
}
