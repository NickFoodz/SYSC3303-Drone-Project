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

        assertFalse(events.isEmpty(), "Expected at least one fire event");
    }

    @Test
    public void testConsumeEvent() {
        fis.consumeEvent(LocalTime.of(13, 0, 0));

        assertEquals(0, fis.allEvents.size());
    }
}