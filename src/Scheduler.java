/**
 * Class Scheduler models a scheduler to receive fire events and dispatch drones
 * @version 1.0
 * @author Nick Fuda
 */

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Scheduler implements Runnable {
    private FireEvent current;
    private boolean shutdownFIS;
    private boolean shutdownDrones;
    private Simulation simulation;
    private ArrayList<FireEvent> eventList = new ArrayList<>();


    /**
     * Constructor for scheduler class
     */
    public Scheduler(Simulation simulation){
        eventList = new ArrayList<>();
        current = null;
        shutdownFIS = false;
        shutdownDrones = false;
        this.simulation = simulation;
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

        //puts HIGH severity FireEvents at the front of the queue, and the rest at the back
        System.out.println("Event added: " + event);
        if(event.getSeverity().equals("High")){
            eventList.addFirst(event);
        } else {
            eventList.addLast(event);
        }
        current = eventList.getFirst();
        notifyAll();
    }

    /**
     * Get the event stored
     * @return the event stored
     */
    public synchronized FireEvent getEvent(){
        while (eventList.isEmpty() && !shutdownDrones) {
            try {
//                System.out.println("Drone waiting for an event...");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        if (!eventList.isEmpty()) {
            FireEvent nextEvent = eventList.removeFirst();
            System.out.println("Drone received event: " + nextEvent);
            return nextEvent;
        }

        return null;
    }

    /**
     * Allows drone thread to shut down from waiting for getEvent()
     */
    public synchronized void shutdown(){
        shutdownDrones = true;
        simulation.endSimulation();
        notifyAll();

    }


    /**
     * Notify all processes that an event was handled, clearing the scheduler for a new event
     * @param event the event that will be reported as complete
     */
    public synchronized void notifyCompletion(FireEvent event){
        System.out.println("Event resolved: " + event);
        if(!eventList.isEmpty()){
            current = eventList.getFirst();
        } else {
            current = null;
        }

        notifyAll();
    }

    @Override
    public void run() {
        while(!shutdownFIS && !Thread.currentThread().isInterrupted()){
            try {
                Thread.sleep(1000);
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
        Simulation simulation = new Simulation(1);
        Scheduler scheduler = new Scheduler(simulation);

        Thread simulationThread = new Thread(simulation);
        Thread schedulerThread = new Thread(scheduler);
        Thread fis = new Thread( new FireIncidentSubsystem(scheduler, simulation));
        Thread drone1 = new Thread(new DroneSubsystem("Drone 1", scheduler));

        simulationThread.start();
        fis.start();
        schedulerThread.start();
        drone1.start();
    }
}
