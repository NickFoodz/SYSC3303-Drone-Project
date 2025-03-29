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
    }

    @Test
    public void testFaultHandling() {

    }
}