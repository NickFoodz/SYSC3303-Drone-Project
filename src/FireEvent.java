public class FireEvent {
    private String time;
    private int zoneID;
    private String type;
    private String severity;

    public FireEvent(String time, int zoneID, String type, String severity){
        this.time = time;
        this.zoneID = zoneID;
        this.type = type;
        this.severity=severity;
    }

    public String getSeverity() {return severity;}
    public String getTime(){return time;}
    public int getZoneID() {return zoneID;}
    public String getType(){return type;}

    @Override
    public String toString() {
        return "[Time: " + time +", Zone: " + zoneID + ", Type: " + type + ", Severity: " + severity + "]";
    }
}
