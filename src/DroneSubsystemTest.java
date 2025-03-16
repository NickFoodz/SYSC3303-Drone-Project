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
        droneSubsystem = new DroneSubsystem("test");
    }

    @Test
    void testInitializeDrones() {
        droneSubsystem.initializeDrones();

        assertEquals(3, droneSubsystem.getDroneList().size());
    }



}