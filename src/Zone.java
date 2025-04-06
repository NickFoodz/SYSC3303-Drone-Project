public class Zone {
    private int id, startX, startY, endX, endY;

    public Zone(int id, int startX, int startY, int endX, int endY){
        this.id = id;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }


    public int[] calculateCenter() {
        int[] center = new int[2];
        center[0] = (endX + startX) / 2;
        center[1] = (endY + startY) / 2;
//        System.out.println("Zone " + id + ": " + center[0] + ", " + center[1]); //testing function
        return center;
    }

    public int getZoneId(){
        return id;
    }

    public int getStartX(){return startX;}
    public int getStartY(){return startY;}
    public int getEndX(){return endX;}
    public int getEndY(){return endY;}


    @Override
    public String toString() {
        return "Zone with ID: " + id;
    }
}
