public class Zone {
    private int id, startX, startY, endX, endY;

    /**
     * Constructor for Zone object
     * @param id - id
     * @param startX - top left x cord
     * @param startY - top left y cord
     * @param endX - bottom right x cord
     * @param endY - bottom right y cord
     */
    public Zone(int id, int startX, int startY, int endX, int endY){
        this.id = id;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    /**
     * Calculate the center of a zone.
     * @return the center of a zone as an int array - [x, y]
     */
    public int[] calculateCenter() {
        int[] center = new int[2];
        center[0] = (endX + startX) / 2;
        center[1] = (endY + startY) / 2;
        return center;
    }

    /**
     * getter for the drones id number
     * @return id
     */
    public int getZoneId(){
        return id;
    }

    /**
     * Getter for the Start X,Y and End X,Y
     */
    public int getStartX(){return startX;}
    public int getStartY(){return startY;}
    public int getEndX(){return endX;}
    public int getEndY(){return endY;}


    /**
     * To String method for zone object
     * @return formatter zone with id
     */
    @Override
    public String toString() {
        return "Zone with ID: " + id;
    }
}
