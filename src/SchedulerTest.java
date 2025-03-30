import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.*;


class SchedulerTest {
    private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new Scheduler();
    }

    @Test
    public void testIncomingMessages() throws IOException, InterruptedException {
        DatagramSocket drone1, drone2, drone3;
        //Ports that drones use
        drone1 = new DatagramSocket(5001);
        drone2 = new DatagramSocket(5002);
        drone3 = new DatagramSocket(5003);
        byte[] buffer = new byte[100];
        String msg = new String("This is a test for scheduler port reception");
        buffer = msg.getBytes();
        DatagramPacket testPacketSend = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), 6001);
        drone1.send(testPacketSend);
        scheduler.receiveMessage();
        assertEquals(scheduler.mostRecentReceivedSocket,5001);
        drone2.send(testPacketSend);
        scheduler.receiveMessage();
        assertEquals(scheduler.mostRecentReceivedSocket, 5002);
        drone3.send(testPacketSend);
        scheduler.receiveMessage();
        assertEquals(scheduler.mostRecentReceivedSocket, 5003);
        scheduler.TESTING_closeSockets();
        drone1.close();
        drone2.close();
        drone3.close();
    }

    @Test
    public void testFaultHandling() throws SocketException {
        DatagramSocket drone1;
        drone1 = new DatagramSocket(5000);

        FireEvent faultyEvent1 = new FireEvent("13:00:05",3,"FIRE_DETECTED","Low", "Drone Stuck");
        FireEvent faultyEvent2 = new FireEvent("14:00:05",3,"FIRE_DETECTED","Low", "Nozzle Jammed");
        FireEvent faultyEvent3 = new FireEvent("15:00:05",3,"FIRE_DETECTED","Low", "Packet Loss/Corrupted Messages");

        scheduler.addEvent(faultyEvent1);
        scheduler.addEvent(faultyEvent2);
        scheduler.addEvent(faultyEvent3);

        assertEquals(scheduler.getEvent().get(0).getFault(), "Drone Stuck");
        assertEquals(scheduler.getEvent().get(1).getFault(), "Nozzle Jammed");
        assertEquals(scheduler.getEvent().get(2).getFault(), "Packet Loss/Corrupted Messages");
        scheduler.TESTING_closeSockets();
        drone1.close();
    }
}