import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {
    private Scheduler scheduler;
    private FireEvent fireEvent;
    private Thread schedulerTestThread;

    @BeforeEach
    void setUp() {
        scheduler = new Scheduler();
        fireEvent = new FireEvent("14:03:15",3,"FIRE_DETECTED","High");
    }

    @Test
    void testScheduler() throws InterruptedException {
        schedulerTestThread = new Thread(scheduler);

        schedulerTestThread.start();

        scheduler.addEvent(fireEvent);

        assertNotNull(scheduler.getEvent());

        scheduler.notifyCompletion(fireEvent);

        // Stop the thread
        scheduler.setShutdownFIS();
        schedulerTestThread.interrupt();

        schedulerTestThread.join();
    }
}