import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DroneSubsystemTest {
    private Scheduler scheduler;
    private FireEvent fireEvent;
    private DroneSubsystem droneSubsystem;
    private Thread droneThread;

    @BeforeEach
    void setUp() {
        scheduler = new Scheduler();
        fireEvent = new FireEvent("14:03:15",3,"FIRE_DETECTED","High");
        droneSubsystem = new DroneSubsystem("Drone1", scheduler);
    }

    @Test
    void testDroneSubsystemHandlesEvent() throws InterruptedException {
        scheduler.addEvent(fireEvent);

        droneThread = new Thread(droneSubsystem);
        droneThread.start();

        Thread.sleep(600); // Allow time for the thread to execute

        assertNull(scheduler.getEvent()); // Ensure event is handled

        droneThread.interrupt(); // Manually interrupt the thread
        droneThread.join(); // Ensure it stops before test ends
    }
}
