import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static org.junit.jupiter.api.Assertions.*;

class DroneSubsystemTest {
    DroneSubsystem droneSubsystem;
    @BeforeEach
    void setUp() {
        droneSubsystem = new DroneSubsystem("testDroneSubsystem");
    }

    @AfterEach
    void takeDown() throws InterruptedException {
        droneSubsystem.TESTING_closeSockets();
        Thread.sleep(100);
    }

    @Test
    void testInitializeDrones() {
        droneSubsystem.initializeDrones();

        assertEquals(3, droneSubsystem.getDroneList().size(), "all drones are initialized");
//        droneSubsystem.TESTING_closeSockets();
    }

    @Test
    void testDroneStateChange() throws InterruptedException {
        droneSubsystem.initializeDrones();
        DroneSubsystem.Drone drone = droneSubsystem.getDroneList().get(0);
        DroneSubsystem.Drone drone2 = droneSubsystem.getDroneList().get(1);

        assertEquals(DroneSubsystem.Drone.droneState.IDLE, drone.getState(), "drone starts in state IDLE");

        FireEvent fireEvent = new FireEvent("13:00:05",3,"FIRE_DETECTED","Low", "null");

        drone.startEvent(fireEvent);

        assertEquals("IDLE", drone.getLog().get(0), "drone starts at IDLE state");
        assertEquals("ENROUTE", drone.getLog().get(1), "drone moves onto ENROUTE state");
        assertEquals("DEPLOYINGAGENT", drone.getLog().get(2), "drone moves onto DEPLOYINGAGENT state");
        assertEquals("RETURNING", drone.getLog().get(3), "drone moves onto RETURNING state");
        assertEquals("IDLE", drone.getLog().get(4), "drone returns to IDLE state");

        //For state with fault
        FireEvent faultEvent = new FireEvent("14:00:00", 2, "FIRE_DETECTED", "High", "Drone Stuck");

        drone.startEvent(faultEvent);

        assertEquals("IDLE", drone.getLog().get(0), "drone starts at IDLE state");
        assertEquals("ENROUTE", drone.getLog().get(1), "drone moves onto ENROUTE state");
        assertEquals("RETURNING", drone.getLog().get(3), "drone moves onto RETURNING state");
        assertEquals("IDLE", drone.getLog().get(4), "drone returns to IDLE state");

        //For nozzle stuck
        FireEvent faultEvent2 = new FireEvent("14:00:00", 2, "FIRE_DETECTED", "High", "Nozzle Jammed");

        drone2.startEvent(faultEvent2);
        assertEquals("IDLE", drone2.getLog().get(0), "drone starts at IDLE state");
        assertEquals("ENROUTE", drone2.getLog().get(1), "drone moves onto ENROUTE state");
        assertEquals("DEPLOYINGAGENT", drone2.getLog().get(2), "drone moves onto DEPLOYINGAGENT state");
        assertEquals("RETURNING", drone.getLog().get(3), "drone moves onto RETURNING state");
        assertEquals("IDLE", drone.getLog().get(4), "drone returns to IDLE state");
        assertEquals("DISABLED", drone2.getLog().get(5), "drone moves onto DISABLED state");



//        droneSubsystem.TESTING_closeSockets();

    }

}