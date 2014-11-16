package km.crawler.multithread;

public class WorkerThread implements Runnable {

    private WorkerTask task;
    private Thread t;

    public WorkerThread(String name, WorkerTask task) {
        this.task = task;
        this.t = new Thread(this, name);
    }
    
    public void start() {
        t.start();
    }

    @Override
    public void run() {
        task.process();
    }
}
