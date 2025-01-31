/**
 * Class Scheduler models a scheduler to receive fire events and dispatch drones
 * @version 1.0
 * @author Nick Fuda
 */

import java.util.ArrayList;

public class Scheduler implements Runnable {
    private FireEvent current = null;

    /**
     * Constructor
     */
    public Scheduler(){}

    public FireEvent getCurrentEvent(){return current;}

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
        current = event;
        notifyAll();
    }

    /**
     * Get the event stored
     * @return the event stored
     */
    public synchronized FireEvent getEvent(){
        while (current == null) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        notifyAll();
        return current;


    }

    /**
     * Notify all processes that an event was handled, clearing the scheduler for a new event
     * @param event the event that will be reported as complete
     */
    public synchronized void notifyCompletion(FireEvent event){
        System.out.println("Event Handled: " + event);
        current = null;
        notifyAll();
    }

    @Override
    public void run() {
        while(true){

        }
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
