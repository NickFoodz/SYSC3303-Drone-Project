public class Launcher {
    public static void main(String[] args) {
        //scheduler
        Simulation simulation = new Simulation(1);
        Scheduler scheduler = new Scheduler(simulation);
        Thread simulationThread = new Thread(simulation);
        Thread schedulerThread = new Thread(scheduler);

        //DS

        //FIS



        Thread fis = new Thread( new FireIncidentSubsystem(scheduler, simulation));
        Thread drone1 = new Thread(new DroneSubsystem("Drone 1", scheduler));

        simulationThread.start();
        fis.start();
        schedulerThread.start();
        drone1.start();
    }
}
