package de.metalcon.zmqworker;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;
import org.zeromq.ZMQ;

/**
 * ZeroMQ worker for backend components using ZeroMQ
 * 
 * @author sebschlicht
 * 
 */
public class ZMQWorker implements Runnable {

    /**
     * number of ZeroMQ thread
     */
    private static int NUM_THREADS = 1;

    /**
     * worker instances in use
     */
    private static int NUM_WORKERS = 0;

    /**
     * ZeroMQ context
     */
    private static ZMQ.Context CONTEXT = ZMQ.context(NUM_THREADS);

    /**
     * ZeroMQ socket to send/receive messages
     */
    private ZMQ.Socket socket;

    /**
     * listener thread
     */
    private Thread thread;

    /**
     * endpoint the worker is listening on
     */
    private String endpoint;

    /**
     * handler for incoming requests
     */
    private ZMQRequestHandler requestHandler;

    /**
     * create new ZeroMQ worker
     * 
     * @param endpoint
     *            endpoint the worker will listen on
     * @param requestHandler
     *            handler for incoming requests
     */
    public ZMQWorker(
            String endpoint,
            ZMQRequestHandler requestHandler) {
        this.endpoint = endpoint;
        this.requestHandler = requestHandler;

        socket = CONTEXT.socket(ZMQ.REP);
        NUM_WORKERS += 1;
        socket.bind(endpoint);
        thread = new Thread(this);
    }

    /**
     * checks if listener thread is alive: started and not died
     * 
     * @return true - if listener thread alive<br>
     *         false otherwise
     */
    public boolean isRunning() {
        return thread.isAlive();
    }

    /**
     * start the worker's listener thread
     * 
     * @return true - if the thread was started<br>
     *         false if already running
     */
    public boolean start() {
        if (!isRunning()) {
            thread.start();
            return true;
        }
        return false;
    }

    /**
     * listener loop
     */
    @Override
    public void run() {
        // TODO: use logging
        System.out.println("i am serving on endpoint: " + endpoint);

        boolean error = false;
        Object request;
        Serializable response;

        try {
            while (true) {
                byte[] serializedRequest = socket.recv();
                if (serializedRequest == null) {
                    // error or shutdown
                    break;
                }

                byte[] serializedResponse = null;
                error = true;
                request = SerializationUtils.deserialize(serializedRequest);

                if (request != null) {
                    response = requestHandler.handleRequest(request);

                    if (response == null) {
                        response = "ERR: unhandled request";
                    } else {
                        serializedResponse =
                                SerializationUtils.serialize(response);
                        if (serializedResponse != null) {
                            error = false;
                        } else {
                            response = "ERR: response serialization failed";
                        }
                    }
                } else {
                    // deserialization failed
                    response = "ERR: request deserialization failed";
                }

                // serialize generated response if an error occurred
                if (error) {
                    serializedResponse = SerializationUtils.serialize(response);
                    if (serializedResponse == null) {
                        throw new IllegalStateException(
                                "serialization method not working");
                    }
                }

                socket.send(serializedResponse);
            }
        } finally {
            cleanUp();
        }
    }

    /**
     * clean up after shutdown
     */
    protected void cleanUp() {
        socket.close();
        NUM_WORKERS -= 1;

        if (NUM_WORKERS == 0) {
            CONTEXT.term();
        }

        thread.interrupt();
    }
}
