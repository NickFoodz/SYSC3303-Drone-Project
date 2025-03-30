import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import static org.junit.jupiter.api.Assertions.*;

class DroneSubsystemTest {
    private DroneSubsystem droneSubsystem = new DroneSubsystem("testDroneSubsystem");

    @Test
    void testInitializeDrones() {
        droneSubsystem.initializeDrones();

        assertEquals(3, droneSubsystem.getDroneList().size(), "all drones are initialized");
        //droneSubsystem.TESTING_closeSockets();
    }

    @Test
    void testDroneStateChange() throws InterruptedException {
        droneSubsystem.initializeDrones();
        DroneSubsystem.Drone drone = droneSubsystem.getDroneList().get(0);

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

        droneSubsystem.TESTING_closeSockets();
    }

    @Test
    void testDroneSendStatus() throws IOException {
        droneSubsystem.initializeDrones();
        DatagramSocket testReceive = new DatagramSocket(6001);
        droneSubsystem.getDroneList().get(0).sendStatus(); //Drone on port 5002
        byte[] acceptData = new byte[100];
        DatagramPacket packetFromDrone = new DatagramPacket(acceptData, acceptData.length);
        testReceive.receive(packetFromDrone);
        assertEquals(packetFromDrone.getPort(), 5002 );
        droneSubsystem.TESTING_closeSockets();
    }

}