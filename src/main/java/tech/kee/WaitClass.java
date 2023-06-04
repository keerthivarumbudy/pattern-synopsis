package tech.kee;

public class WaitClass {
    public void waitUntilInterrupt(){
        while(true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
