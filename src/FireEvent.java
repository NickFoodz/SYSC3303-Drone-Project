/**
 * Class FireEvent is a structure class to hold the data of an event
 * @version 1.0
 * @author Nick Fuda
 */
public class FireEvent {
    private String time;
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
        this.time = time;
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
     * Getter for the time
     * @return time as a string
     */
    public String getTime(){return time;}

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
        return "[Time: " + time +", Zone: " + zoneID + ", Type: " + type + ", Severity: " + severity + "]";
    }
}
