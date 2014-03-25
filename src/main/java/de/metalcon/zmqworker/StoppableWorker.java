package de.metalcon.zmqworker;

/**
 * basic stoppable worker
 * 
 * @author sebschlicht
 * 
 */
public abstract class StoppableWorker implements Runnable {

    /**
     * worker thread
     */
    private Thread workerThread;

    /**
     * worker status flag
     */
    protected boolean running;

    /**
     * worker stopping flag
     */
    protected boolean stopping;

    /**
     * start the worker thread
     * 
     * @return true - if worker was started<br>
     *         false if already running
     */
    public boolean start() {
        if (!running) {
            workerThread = new Thread(this);
            System.out.println("starting worker thread");
            workerThread.start();
            System.out.println("worker thread started");
            return true;
        }
        return false;
    }

    /**
     * stop the worker thread
     */
    public void stop() {
        if (running) {
            stopping = true;
        }
    }

    /**
     * wait until the worker has shut down
     */
    public void waitForShutdown() {
        // wait for the worker thread to finish
        while (running) {
            try {
                workerThread.join();
            } catch (InterruptedException e) {
                // interrupted while waiting, retry
            }
        }
    }

    /**
     * access worker status flag
     * 
     * @return worker status flag
     */
    public boolean isRunning() {
        return running;
    }

}
