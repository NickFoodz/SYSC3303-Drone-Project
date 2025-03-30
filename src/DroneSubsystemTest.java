import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DroneSubsystemTest {
    private DroneSubsystem droneSubsystem = new DroneSubsystem("testDroneSubsystem");

    @Test
    void testInitializeDrones() {
        droneSubsystem.initializeDrones();
        assertEquals(3, droneSubsystem.getDroneList().size(), "all drones are initialized");
        droneSubsystem.TESTING_closeSockets();
    }

    @Test
    void testDroneStateChange() throws InterruptedException {
        droneSubsystem.initializeDrones();
        DroneSubsystem.Drone drone = droneSubsystem.getDroneList().get(0);
        DroneSubsystem.Drone drone2 = droneSubsystem.getDroneList().get(1);
        DroneSubsystem.Drone drone3 = droneSubsystem.getDroneList().get(2);

        assertEquals(DroneSubsystem.Drone.droneState.IDLE, drone.getState(), "drone starts in state IDLE");

        //Regular event
        FireEvent fireEvent = new FireEvent("13:00:05",3,"FIRE_DETECTED","Low", "null");

        drone.startEvent(fireEvent);

        assertEquals("IDLE", drone.getLog().get(0), "drone starts at IDLE state");
        assertEquals("ENROUTE", drone.getLog().get(1), "drone moves onto ENROUTE state");
        assertEquals("DEPLOYINGAGENT", drone.getLog().get(2), "drone moves onto DEPLOYINGAGENT state");
        assertEquals("RETURNING", drone.getLog().get(3), "drone moves onto RETURNING state");
        assertEquals("IDLE", drone.getLog().get(4), "drone returns to IDLE state");

        //Stuck event
        FireEvent stuckEvent = new FireEvent("14:00:00",3,"FIRE_DETECTED","Low", "Drone Stuck");
        drone2.startEvent(stuckEvent);
        assertEquals("IDLE", drone2.getLog().get(0), "drone starts at IDLE state");
        assertEquals("ENROUTE", drone2.getLog().get(1), "drone moves onto ENROUTE state");
        assertEquals("RETURNING", drone2.getLog().get(2), "drone moves onto RETURNING state");
        assertEquals("IDLE", drone2.getLog().get(3), "drone returns to IDLE state");

        //Jammed Event
        FireEvent nozzleEvent = new FireEvent("14:00:00",3,"FIRE_DETECTED","Low", "Nozzle Jammed");
        drone3.startEvent(nozzleEvent);
        assertEquals("IDLE", drone3.getLog().get(0), "drone starts at IDLE state");
        assertEquals("ENROUTE", drone3.getLog().get(1), "drone moves onto ENROUTE state");
        assertEquals("DEPLOYINGAGENT", drone3.getLog().get(2), "drone moves onto DEPLOYINGAGENT state");
        assertEquals("RETURNING", drone3.getLog().get(3), "drone moves onto RETURNING state");
        assertEquals("IDLE", drone3.getLog().get(4), "drone returns to IDLE state");
        assertEquals("DISABLED", drone3.getLog().get(5), "drone moves onto DISABLED state");
        droneSubsystem.TESTING_closeSockets();




    }
}