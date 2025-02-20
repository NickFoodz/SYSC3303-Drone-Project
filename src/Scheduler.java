/**
 * Class Scheduler models a scheduler to receive fire events and dispatch drones
 * @version 1.0
 * @author Nick Fuda
 */

import java.util.concurrent.ConcurrentLinkedQueue;

public class Scheduler implements Runnable {
    private FireEvent current;
    private boolean shutdownFIS;
    private boolean shutdownDrones;
    private ConcurrentLinkedQueue<FireEvent> eventList;


    /**
     * Constructor for scheduler class
     */
    public Scheduler(){
        eventList = new ConcurrentLinkedQueue<>();
        current = null;
        shutdownFIS = false;
        shutdownDrones = false;
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
        System.out.println("Event added: " + event);
        eventList.add(event);
        current = eventList.peek();
        notifyAll();
    }

    /**
     * Get the event stored
     * @return the event stored
     */
    public synchronized FireEvent getEvent(){
        while (current == null && !shutdownDrones) {
            try {
                //Drone is waiting for event or shutdown
                wait();
            } catch (InterruptedException e) {
            }
        }
        notifyAll();
        return current;
    }

    /**
     * Allows drone thread to shut down from waiting for getEvent()
     */
    public synchronized void shutdown(){
        shutdownDrones = true;
        notifyAll();

    }


    /**
     * Notify all processes that an event was handled, clearing the scheduler for a new event
     * @param event the event that will be reported as complete
     */
    public synchronized void notifyCompletion(FireEvent event){
        System.out.println("Event resolved: " + event);
        eventList.remove();
        current = eventList.peek();

        notifyAll();
    }

    @Override
    public void run() {
        while(!shutdownFIS && !Thread.currentThread().isInterrupted()){
            try {
                Thread.sleep(500);
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

        Scheduler scheduler = new Scheduler();
        Thread schedulerThread = new Thread(scheduler);
        Thread fis = new Thread( new FireIncidentSubsystem(scheduler));
        Thread drone1 = new Thread(new DroneSubsystem("Drone 1", scheduler));


        schedulerThread.start();
        drone1.start();
        fis.start();
    }
}
