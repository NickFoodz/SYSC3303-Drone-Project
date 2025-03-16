import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.DatagramSocket;
import java.net.DatagramPacket;


class SchedulerTest {
    private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new Scheduler();
    }

    @Test
    public void testAddEvent_HighSeverity_FirstInQueue() {
        FireEvent lowEvent = new FireEvent("12:00", 1, "Forest", "Low");
        FireEvent highEvent = new FireEvent("12:05", 2, "Building", "High");

        scheduler.addEvent(lowEvent);
        scheduler.addEvent(highEvent);

        assertEquals(highEvent, scheduler.getEvent().getFirst());
        scheduler.TESTING_closeSockets();
    }

    @Test
    public void testAddEvent_LowSeverity_AddedToBack() {
        FireEvent event1 = new FireEvent("12:00", 1, "Forest", "Low");
        FireEvent event2 = new FireEvent("12:05", 2, "Building", "Low");

        scheduler.addEvent(event1);
        scheduler.addEvent(event2);

        assertEquals(event2, scheduler.getEvent().getLast());
        scheduler.TESTING_closeSockets();
    }

    @Test
    public void testNotifyAcceptance_UpdatesCurrentEvent() {
        FireEvent event1 = new FireEvent("12:00", 1, "Forest", "Low");
        FireEvent event2 = new FireEvent("12:05", 2, "Building", "High");

        scheduler.addEvent(event1);
        scheduler.addEvent(event2);

        scheduler.notifyAcceptance(event2);

        assertEquals(event1, scheduler.getCurrentEvent());
        scheduler.TESTING_closeSockets();
    }
}