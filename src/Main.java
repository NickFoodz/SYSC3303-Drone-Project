public class Main {
    public static void main(String[] args) {
        Scheduler s = new Scheduler();
        Thread fis = new Thread( new FireIncidentSubsystem(s, "Sample_event_file.csv"));

        fis.start();
    }
}