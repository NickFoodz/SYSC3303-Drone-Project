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
        fireIncidentSubsystem = new FireIncidentSubsystem();
    }

    @Test
    void testInitializeDrones() {
        assertEquals(1, droneSubsystem.getDroneList().size());
    }

    @Test
    void testFightFire() throws InterruptedException {
        simulationTestThread = new Thread(simulation);


        simulationTestThread.start();
        schedulerTestThread.start();
        fireIncidentSubsystemTestThread.start();
        droneSubSystemTestThread.start();

        Thread.sleep(6000);

        assertEquals(1, droneSubsystem.getDroneList().size(), "All drones have returned");
        assertNull(scheduler.getEvent(), "All events are handled");

        // Stop the thread
        droneSubSystemTestThread.interrupt();
        fireIncidentSubsystemTestThread.interrupt();
        schedulerTestThread.interrupt();
        simulationTestThread.interrupt();
        fireIncidentSubsystemTestThread.join();
        droneSubSystemTestThread.join();
        schedulerTestThread.join();
        simulationTestThread.join();
    }
}