import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DroneSubsystemTest {
    private Scheduler scheduler;
    private FireIncidentSubsystem fireIncidentSubsystem;
    private DroneSubsystem droneSubsystem;
    private Simulation simulation;
    private Thread simulationTestThread;
    private Thread fireIncidentSubsystemTestThread;
    private Thread droneSubSystemTestThread;
    private Thread schedulerTestThread;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1);
        scheduler = new Scheduler(simulation);
        fireIncidentSubsystem = new FireIncidentSubsystem(scheduler, simulation);
        droneSubsystem = new DroneSubsystem("drone1", scheduler);
    }

    @Test
    void testFightFire() throws InterruptedException {
        simulationTestThread = new Thread(simulation);
        fireIncidentSubsystemTestThread = new Thread(fireIncidentSubsystem);
        droneSubSystemTestThread = new Thread(droneSubsystem);
        schedulerTestThread = new Thread(scheduler);

        simulationTestThread.start();
        schedulerTestThread.start();
        fireIncidentSubsystemTestThread.start();
        droneSubSystemTestThread.start();

        Thread.sleep(6000);

        assertNull(scheduler.getEvent()); // Ensure event is handled

        // Stop the thread
        droneSubSystemTestThread.interrupt();
        fireIncidentSubsystemTestThread.interrupt();
        schedulerTestThread.interrupt();
        fireIncidentSubsystemTestThread.join();
        droneSubSystemTestThread.join();
        schedulerTestThread.join();
    }
    @Test
    void testPutOutFire(){
        DroneSubsystem drone = new DroneSubsystem("Test", scheduler);
        FireEvent testSevereEvent = new FireEvent("00:00:00",0,"TEST", "High");
        FireEvent testModerateEvent = new FireEvent("00:00:00",0,"TEST", "Moderate");
        FireEvent testLowEvent = new FireEvent("00:00:00",0,"TEST", "Low");
        //assertEquals(drone.putOutFire(testSevereEvent), 30);
        //assertEquals(drone.putOutFire(testModerateEvent), 20);
        //assertEquals(drone.putOutFire(testLowEvent), 10);
    }

}
