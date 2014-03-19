package de.metalcon.zmqworker;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;
import org.zeromq.ZMQ;

import de.metalcon.zmqworker.responses.errors.ParsingErrorResponse;

/**
 * ZeroMQ worker for backend components using ZeroMQ
 * 
 * @author sebschlicht
 * 
 */
public class ZMQWorker implements Runnable {

    /**
     * number of ZeroMQ threads
     */
    private static int NUM_THREADS = 1;

    /**
     * worker instances in use
     */
    private static int NUM_WORKERS = 0;

    /**
     * ZeroMQ context
     */
    private static ZMQ.Context CONTEXT;//TODO = ZMQ.context(NUM_THREADS);

    /**
     * ZeroMQ socket to receive messages
     */
    private ZMQ.Socket socketReceive;

    /**
     * endpoint the worker will receive requests on
     */
    private String endpointReceive;

    /**
     * endpoint the worker will send responses on
     */
    private String endpointSend;

    /**
     * ZeroMQ socket to send messages
     */
    private ZMQ.Socket socketSend;

    /**
     * listener thread
     */
    private Thread thread;

    /**
     * handler for incoming requests
     */
    private ZMQRequestHandler requestHandler;

    /**
     * create new ZeroMQ worker
     * 
     * @param endpointReceive
     *            endpoint the worker will receive requests on
     * @param endpointSend
     *            endpoint the worker will send responses on
     * @param requestHandler
     *            handler for incoming requests
     */
    public ZMQWorker(
            String endpointReceive,
            String endpointSend,
            ZMQRequestHandler requestHandler,
            ZMQ.Context context) {
        this.endpointReceive = endpointReceive;
        this.endpointSend = endpointSend;
        this.requestHandler = requestHandler;

        // TODO: remove
        CONTEXT = context;
        socketReceive = CONTEXT.socket(ZMQ.PULL);
        socketSend = CONTEXT.socket(ZMQ.PUSH);

        NUM_WORKERS += 1;
        socketReceive.bind(endpointReceive);
        socketReceive.setSendBufferSize(100000);
        socketReceive.setHWM(100000);

        socketSend.bind(endpointSend);
        socketSend.setSendBufferSize(100000);
        socketSend.setHWM(100000);
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
        System.out.println("i am receiving on endpoint: " + endpointReceive);
        System.out.println("i am sending on endpoint: " + endpointSend);

        boolean error = false;
        Object request;
        Serializable response;

        try {
            while (true) {
                byte[] serializedRequest = socketReceive.recv();
                if (serializedRequest == null) {
                    // error or shutdown
                    break;
                }

                byte[] serializedResponse = null;
                error = true;
                request = SerializationUtils.deserialize(serializedRequest);

                if (request != null) {
                    response = requestHandler.handleRequest(request);

                    if (response != null) {
                        serializedResponse =
                                SerializationUtils.serialize(response);
                        if (serializedResponse != null) {
                            error = false;
                        } else {
                            response =
                                    new ParsingErrorResponse(
                                            "response serialization failed",
                                            null);
                        }
                    } else {
                        throw new IllegalStateException(
                                "request handler returned null");
                    }
                } else {
                    // deserialization failed
                    response =
                            new ParsingErrorResponse(
                                    "request deserialization failed",
                                    "request has to implement Serializable");
                }

                // serialize generated response if an error occurred
                if (error) {
                    serializedResponse = SerializationUtils.serialize(response);
                    if (serializedResponse == null) {
                        throw new IllegalStateException(
                                "serialization method not working");
                    }
                }

                socketSend.send(serializedResponse);
            }
        } finally {
            cleanUp();
        }
    }

    /**
     * clean up after shutdown
     */
    protected void cleanUp() {
        socketSend.close();
        NUM_WORKERS -= 1;

        if (NUM_WORKERS == 0) {
            CONTEXT.term();
        }

        thread.interrupt();
    }
}
