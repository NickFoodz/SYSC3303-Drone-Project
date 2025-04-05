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
    private String fault;
    private Zone zone;

    /**
     * Constructor for FireEvent, usually information comes from the FireIncidentSubsystem
     * @param time time as a String
     * @param zoneID id of a zone as an int
     * @param type type of event that occurred as a string
     * @param severity the severity as a string
     */

    public FireEvent(String time, int zoneID, String type, String severity, String fault) {
        this.time = time;
        this.zoneID = zoneID;
        this.type = type;
        this.severity=severity;
        this.fault = fault;
        this.zone = null;
    }
    public FireEvent(String time, int zoneID, String type, String severity, String fault, Zone zone) {
        this.time = time;
        this.zoneID = zoneID;
        this.type = type;
        this.severity=severity;
        this.fault = fault;
        this.zone = zone;
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
     * Getter for the fault
     * @return fault as an int
     */
    public String getFault() {return fault;}

    /**
     * Getter for the zone
     * @return zone
     */
    public Zone getZone() {return zone;}

    /**
     * Setter for the fault after it has been handled
     */
    public void clearFault() {fault = "null";}

    /**
     * Getter for the type of event
     * @return event type as a string
     */
    public String getType(){return type;}

    /**
     * Summarizes event in a string to send via UDP
     * @return a string in the form Time:ZoneID:Type:Severity
     */
    public String summarizeEvent(){
        return new String(this.getTime()+","+ this.getZoneID()+","+ this.getType()+","+ this.getSeverity()+","+ this.getFault());
    }

    @Override
    public String toString() {
        if(zone != null){
            return "[Time: " + time +", Zone: " + zoneID + ", Type: " + type + ", Severity: " + severity + ", Fault: " + fault + ", Zone: " + zone + "]";
        }
        return "[Time: " + time +", Zone: " + zoneID + ", Type: " + type + ", Severity: " + severity + ", Fault: " + fault + "]";
    }
}
