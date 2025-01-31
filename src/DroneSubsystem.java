
public class DroneSubsystem implements Runnable{
    private String name;
    private final Scheduler scheduler;

    public DroneSubsystem(String name, Scheduler s){
        this.name = name;
        this.scheduler = s;
    }

    @Override
    public void run() {
        try{
            while(true){
            FireEvent current = scheduler.getEvent();
            if(current != null){
                System.out.println(name + " assigned to event: " + current);
                Thread.sleep(2000);
                scheduler.notifyCompletion(current);
            }
            else{
                Thread.sleep(500);
            }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
