import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FireIncidentSubsystemTest {
    private Scheduler scheduler;
    private FireIncidentSubsystem fireIncidentSubsystem;
    private Simulation simulation;
    private DroneSubsystem droneSubsystem;
    private Thread simulationTestThread;
    private Thread fireIncidentSubsystemTestThread;
    private Thread droneSubSystemTestThread;
    private Thread schedulerTestThread;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(20);

        fireIncidentSubsystem = new FireIncidentSubsystem();
    }

    @Test
    void testGetData() throws InterruptedException {
        simulationTestThread = new Thread(simulation);
        fireIncidentSubsystemTestThread = new Thread();
        schedulerTestThread = new Thread();

        simulationTestThread.start();
        schedulerTestThread.start();
        fireIncidentSubsystemTestThread.start();
        droneSubSystemTestThread.start();

        Thread.sleep(6000);

        assertTrue(fireIncidentSubsystem.EOF);

        // Stop the thread
        droneSubSystemTestThread.interrupt();
        fireIncidentSubsystemTestThread.interrupt();
        schedulerTestThread.interrupt();
        fireIncidentSubsystemTestThread.join();
        droneSubSystemTestThread.join();
        schedulerTestThread.join();

    }
}