import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Class FireEvent is a structure class to hold the data of an event
 * @version 1.0
 * @author Nick Fuda
 */
public class FireEvent {
    private LocalTime time;
    private int zoneID;
    private String type;
    private String severity;

    /**
     * Constructor for FireEvent, usually information comes from the FireIncidentSubsystem
     * @param time time as a String
     * @param zoneID id of a zone as an int
     * @param type type of event that occurred as a string
     * @param severity the severity as a string
     */
    public FireEvent(String time, int zoneID, String type, String severity){
        this.time = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm:ss"));;
        this.zoneID = zoneID;
        this.type = type;
        this.severity=severity;
    }

    /**
     * Getter for the severity
     * @return the severity string
     */
    public String getSeverity() {return severity;}

    /**
     * Getter for the severity number
     * High -> 3
     * Moderate -> 2
     * Low -> 1
     *
     * @return the severity number
     */
    public int getSeverityLevel() {
        int sev = 1;
        if(severity.equals("High")){
            sev = 3;
        } else if (severity.equals("Moderate")) {
            sev = 2;
        } else if (severity.equals("Low")) {
            sev = 1;
        }
        return sev;
    }

    /**
     * Getter for the time
     * @return time as a string
     */
    public LocalTime getTime(){return time;}

    /**
     * Getter for the zone ID
     * @return zone ID as an int
     */
    public int getZoneID() {return zoneID;}

    /**
     * Getter for the type of event
     * @return event type as a string
     */
    public String getType(){return type;}

    @Override
    public String toString() {
        DateTimeFormatter alwaysShowSeconds = DateTimeFormatter.ofPattern("HH:mm:ss");
        return "[Time: " + time.format(alwaysShowSeconds) +", Zone: " + zoneID + ", Type: " + type + ", Severity: " + severity + "]";
    }
}
