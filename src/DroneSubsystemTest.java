import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DroneSubsystemTest {
    private DroneSubsystem droneSubsystem = new DroneSubsystem("testDroneSubsystem");

    @Test
    void testInitializeDrones() {
        droneSubsystem.initializeDrones();

        assertEquals(3, droneSubsystem.getDroneList().size(), "all drones are initialized");
    }

    @Test
    void testDroneStateChange() throws InterruptedException {
        droneSubsystem.initializeDrones();
        DroneSubsystem.Drone drone = droneSubsystem.getDroneList().get(0);

        assertEquals(DroneSubsystem.Drone.droneState.IDLE, drone.getState(), "drone starts in state IDLE");

        FireEvent fireEvent = new FireEvent("13:00:05",3,"FIRE_DETECTED","Low");

        drone.startEvent(fireEvent);

        assertEquals("IDLE", drone.getLog().get(0), "drone starts at IDLE state");
        assertEquals("ENROUTE", drone.getLog().get(1), "drone moves onto ENROUTE state");
        assertEquals("DEPLOYINGAGENT", drone.getLog().get(2), "drone moves onto DEPLOYINGAGENT state");
        assertEquals("RETURNING", drone.getLog().get(3), "drone moves onto RETURNING state");
        assertEquals("IDLE", drone.getLog().get(4), "drone returns to IDLE state");
    }
}