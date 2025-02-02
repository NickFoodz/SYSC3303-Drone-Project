import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FireIncidentSubsystemTest {
    private Scheduler scheduler;
    private FireIncidentSubsystem fireIncidentSubsystem;
    private DroneSubsystem droneSubsystem;
    private Thread fireIncidentSubsystemTestThread;
    private Thread droneSubSystemTestThread;

    @BeforeEach
    void setUp() {
        scheduler = new Scheduler();
        fireIncidentSubsystem = new FireIncidentSubsystem(scheduler);
        droneSubsystem = new DroneSubsystem("drone1", scheduler);
    }

    @Test
    void testFireIncidentSubsystemReadsAllEvents() throws InterruptedException {
        fireIncidentSubsystemTestThread = new Thread(fireIncidentSubsystem);
        droneSubSystemTestThread = new Thread(droneSubsystem);
        fireIncidentSubsystemTestThread.start();
        droneSubSystemTestThread.start();

        Thread.sleep(6000);

        assertTrue(fireIncidentSubsystem.EOF);

        // Stop the thread
        fireIncidentSubsystemTestThread.interrupt();
        droneSubSystemTestThread.interrupt();
        fireIncidentSubsystemTestThread.join();
        droneSubSystemTestThread.join();

    }
}