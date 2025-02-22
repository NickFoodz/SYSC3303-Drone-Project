import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DroneSubsystemTest {
    private Scheduler scheduler;
    private FireIncidentSubsystem fireIncidentSubsystem;
    private DroneSubsystem droneSubsystem;
    private FireEvent fireEvent;
    private Thread fireIncidentSubsystemTestThread;
    private Thread droneSubSystemTestThread;
    private Thread schedulerTestThread;

    @BeforeEach
    void setUp() {
        scheduler = new Scheduler();
        droneSubsystem = new DroneSubsystem("testDroneSubsystem", scheduler);
        fireIncidentSubsystem = new FireIncidentSubsystem(scheduler);
    }

    @Test
    void testInitializeDrones() {
        assertEquals(1, droneSubsystem.getDroneList().size());
    }

    @Test
    void testFightFire() throws InterruptedException {
        fireIncidentSubsystemTestThread = new Thread(fireIncidentSubsystem);
        droneSubSystemTestThread = new Thread(droneSubsystem);
        schedulerTestThread = new Thread(scheduler);

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
        fireIncidentSubsystemTestThread.join();
        droneSubSystemTestThread.join();
        schedulerTestThread.join();
    }
}
