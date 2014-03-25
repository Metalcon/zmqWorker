package de.metalcon.zmqworker;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;
import org.zeromq.ZMQ;

import de.metalcon.api.responses.errors.ParsingErrorResponse;

/**
 * ZeroMQ worker for backend components using ZeroMQ
 * 
 * @author sebschlicht
 * 
 */
public class ZMQWorker extends StoppableWorker {

    /**
     * worker instances in use
     */
    private static int NUM_WORKERS = 0;

    /**
     * ZeroMQ context
     */
    private static ZMQ.Context CONTEXT;

    /**
     * ZeroMQ socket to receive/send messages
     */
    private ZMQ.Socket socket;

    /**
     * endpoint the worker will receive/send requests on
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
     *            endpoint the worker will serve on
     * @param requestHandler
     *            handler for incoming requests
     */
    public ZMQWorker(
            String endpoint,
            ZMQRequestHandler requestHandler,
            ZMQ.Context context) {
        this.endpoint = endpoint;
        this.requestHandler = requestHandler;

        CONTEXT = context;
    }

    @Override
    public boolean start() {
        if (super.start()) {
            NUM_WORKERS += 1;

            socket = CONTEXT.socket(ZMQ.ROUTER);
            socket.bind(endpoint);
            socket.setSendBufferSize(100000);
            socket.setHWM(100000);

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
        running = true;

        boolean error = false;
        Object request;
        Serializable response;

        try {
            while (!stopping) {
                byte[] clientId = socket.recv();
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

                socket.send(clientId, ZMQ.SNDMORE);
                socket.send(serializedResponse);
            }
        } finally {
            running = false;
            cleanUp();
        }
    }

    /**
     * stop the ZMQ worker and wait for its shutdown
     */
    @Override
    public void stop() {
        super.stop();

        // close socket to leave listener loop
        socket.close();

        waitForShutdown();
    }

    /**
     * clean up after shutdown
     */
    protected void cleanUp() {
        // TODO: close socket if not closed yet

        NUM_WORKERS -= 1;
        if (NUM_WORKERS == 0) {
            CONTEXT.term();
        }
    }
}
