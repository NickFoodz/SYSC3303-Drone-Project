import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.io.*;
import java.util.List;
import java.time.LocalTime;

class FireIncidentSubsystemTest {
    private FireIncidentSubsystem fis;
    private final String testFilePath = "test_events.csv";

    @BeforeEach
    public void setUp() throws IOException {
        fis = new FireIncidentSubsystem();
    }

    @Test
    public void testGetData() {
        fis.getData();
        List<FireEvent> events = fis.allEvents;

        assertEquals(4, events.size());
        assertEquals("13:00:05", events.get(0).getTime());
        assertEquals("13:03:15", events.get(1).getTime());
        assertEquals("14:03:15", events.get(2).getTime());
        assertEquals("14:10:00", events.get(3).getTime());
    }

    @Test
    public void testConsumeEvent() {
        fis.consumeEvent(LocalTime.of(13, 0, 0));

        assertEquals(0, fis.allEvents.size());
    }
}