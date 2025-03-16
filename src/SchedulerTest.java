import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {
    private Scheduler scheduler;
    private Simulation simulation;
    private FireEvent fireEvent;
    private Thread schedulerTestThread;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(20);
        scheduler = new Scheduler();
        fireEvent = new FireEvent("14:03:15",3,"FIRE_DETECTED","High");
    }

    @Test
    void testScheduler() throws InterruptedException {
    }
}