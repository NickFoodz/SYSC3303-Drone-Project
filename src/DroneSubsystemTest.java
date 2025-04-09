import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

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
        Thread.sleep(250);
    }

    @Test
    void testInitializeDrones() {
        droneSubsystem.initializeDrones();

        assertEquals(droneSubsystem.getDroneNum(), droneSubsystem.getDroneList().size(), "all drones are initialized");
//        droneSubsystem.TESTING_closeSockets();
    }

    @Test
    void testPacketLossFault() throws InterruptedException {
        Scheduler s = new Scheduler();
        s.initializeSendReceiveThreads();

        droneSubsystem.initializeDrones();

        Zone zone3 = new Zone(2, 700, 0, 2000, 600);
        FireEvent fireEvent = new FireEvent("13:00:05", 3, "FIRE_DETECTED", "Low", "Packet Loss/Corrupted Messages", zone3);
        s.addEvent(fireEvent);

        Thread droneThread = new Thread(() -> {
            droneSubsystem.manageDrones();
        });
        droneThread.start();

        while (!s.getTESTING_rejectionHandled()) {
            Thread.sleep(100);
        }

        assertTrue(s.getTESTING_rejectionHandled(), "Scheduler should have handled the rejection");

        droneThread.interrupt();
    }

    @Test
    void testDroneStuckFault() throws InterruptedException {
        droneSubsystem.initializeDrones();
        DroneSubsystem.Drone drone = droneSubsystem.getDroneList().get(0);

        assertEquals(DroneSubsystem.Drone.droneState.IDLE, drone.getState(), "drone starts in state IDLE");

        Zone zone2 = new Zone(3, 0, 600, 1300, 1500);
        FireEvent faultEvent = new FireEvent("14:00:00", 2, "FIRE_DETECTED", "High", "Drone Stuck", zone2);

        drone.startEvent(faultEvent);

        assertTrue(drone.getTESTING_StuckHandled(), "Drone should have handled the Stuck Exception");

    }

    @Test
    void testNozzleJammedFault() throws InterruptedException {
        droneSubsystem.initializeDrones();
        DroneSubsystem.Drone drone2 = droneSubsystem.getDroneList().get(1);

        assertEquals(DroneSubsystem.Drone.droneState.IDLE, drone2.getState(), "drone starts in state IDLE");

        Zone zone2 = new Zone(3, 0, 600, 1300, 1500);
        FireEvent faultEvent2 = new FireEvent("14:00:00", 2, "FIRE_DETECTED", "High", "Nozzle Jammed", zone2);

        drone2.startEvent(faultEvent2);

        assertTrue(drone2.getTESTING_NozzleHandled(), "Drone should have handled the Nozzle Jammed Exception");

    }

    @Test
    void testAgentUsed() throws InterruptedException {
        droneSubsystem.initializeDrones();
        DroneSubsystem.Drone drone2 = droneSubsystem.getDroneList().get(1);

        assertEquals(DroneSubsystem.Drone.droneState.IDLE, drone2.getState(), "drone starts in state IDLE");

        Zone zone2 = new Zone(3, 0, 600, 1300, 1500);
        FireEvent faultEvent2 = new FireEvent("14:00:00", 2, "FIRE_DETECTED", "Low", "null", zone2);

        drone2.startEvent(faultEvent2);

        assertEquals(10, drone2.getTotalAgentSprayed(), "Shouldve used 10L of agent");
    }

    @Test
    void testDroneStateChange() throws InterruptedException {
        droneSubsystem.initializeDrones();
        DroneSubsystem.Drone drone = droneSubsystem.getDroneList().get(0);
        DroneSubsystem.Drone drone2 = droneSubsystem.getDroneList().get(1);

        assertEquals(DroneSubsystem.Drone.droneState.IDLE, drone.getState(), "drone starts in state IDLE");

        Zone zone3 = new Zone(2, 700, 0, 2000, 600);
        Zone zone2 = new Zone(3, 0, 600, 1300, 1500);

        FireEvent fireEvent = new FireEvent("13:00:05",3,"FIRE_DETECTED","Low", "Packet Loss/Corrupted Messages", zone3);

        drone.startEvent(fireEvent);

        assertEquals("IDLE", drone.getLog().get(0), "drone starts at IDLE state");
        assertEquals("ENROUTE", drone.getLog().get(1), "drone moves onto ENROUTE state");
        assertEquals("DEPLOYINGAGENT", drone.getLog().get(2), "drone moves onto DEPLOYINGAGENT state");
        assertEquals("RETURNING", drone.getLog().get(3), "drone moves onto RETURNING state");
        assertEquals("IDLE", drone.getLog().get(4), "drone returns to IDLE state");

        //For state with fault
        FireEvent faultEvent = new FireEvent("14:00:00", 2, "FIRE_DETECTED", "High", "Drone Stuck", zone2);

        drone.startEvent(faultEvent);

        assertEquals("IDLE", drone.getLog().get(0), "drone starts at IDLE state");
        assertEquals("ENROUTE", drone.getLog().get(1), "drone moves onto ENROUTE state");
        assertEquals("RETURNING", drone.getLog().get(3), "drone moves onto RETURNING state");
        assertEquals("IDLE", drone.getLog().get(4), "drone returns to IDLE state");

        //For nozzle stuck
        FireEvent faultEvent2 = new FireEvent("14:00:00", 2, "FIRE_DETECTED", "High", "Nozzle Jammed", zone2);

        drone2.startEvent(faultEvent2);
        assertEquals("IDLE", drone2.getLog().get(0), "drone starts at IDLE state");
        assertEquals("ENROUTE", drone2.getLog().get(1), "drone moves onto ENROUTE state");
        assertEquals("DEPLOYINGAGENT", drone2.getLog().get(2), "drone moves onto DEPLOYINGAGENT state");
        assertEquals("RETURNING", drone.getLog().get(3), "drone moves onto RETURNING state");
        assertEquals("IDLE", drone.getLog().get(4), "drone returns to IDLE state");
        assertEquals("DISABLED", drone2.getLog().get(5), "drone moves onto DISABLED state");



//        droneSubsystem.TESTING_closeSockets();

    }

    @Test
    void testRerouteEvents() throws InterruptedException {
        droneSubsystem.initializeDrones();
        DroneSubsystem.Drone drone = droneSubsystem.getDroneList().get(0);

        FireEvent fireEvent1 = new FireEvent("13:00:05",4,"FIRE_DETECTED","Low", "null");
        FireEvent fireEvent2 = new FireEvent("13:00:06",3,"FIRE_DETECTED","Low", "null");


        droneSubsystem.assignDrone(fireEvent1.summarizeEvent());
        Thread.sleep(5000);
        droneSubsystem.assignDrone(fireEvent2.summarizeEvent());
        Thread.sleep(40000);
        System.out.println("\nLOG: " + drone.getLog());



    }

}