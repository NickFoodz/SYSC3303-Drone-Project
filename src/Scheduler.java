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
     * Adds an event to the
     * @param event
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
        Thread fis = new Thread( new FireIncidentSubsystem(scheduler, "Sample_event_file.csv"));
        Thread drone1 = new Thread(new DroneSubsystem("Drone 1", scheduler));


        schedulerThread.start();
        drone1.start();
        fis.start();
    }
}
