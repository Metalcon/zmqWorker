package de.metalcon.zmqworker;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;
import org.zeromq.ZContext;
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
     * ZeroMQ context<br>
     * now ZContext (https://github.com/zeromq/jeromq/issues/140)
     */
    private static ZContext CONTEXT;

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
     * @param context
     *            ZMQ context to use<br>
     *            set to <b>null</b> to use the context set (creates a new one
     *            if none set)
     * @throws IllegalStateException
     *             if different context set previously, which is still in use
     */
    public ZMQWorker(
            String endpoint,
            ZMQRequestHandler requestHandler,
            ZContext context) {
        this.endpoint = endpoint;
        this.requestHandler = requestHandler;
        if (context == null) {
            if (CONTEXT == null) {
                // TODO log: no context specified, using new context
                context = new ZContext();
            }
            // reuse current context else
        } else if (CONTEXT != null && CONTEXT != context && NUM_WORKERS > 0) {
            throw new IllegalStateException(
                    "different context already set and still in use");
        }
        CONTEXT = context;
    }

    /**
     * create new ZeroMQ worker<br>
     * uses the context set or creates a new one
     * 
     * @param endpoint
     *            endpoint the worker will serve on
     * @param requestHandler
     *            handler for incoming requests
     */
    public ZMQWorker(
            String endpoint,
            ZMQRequestHandler requestHandler) {
        this(endpoint, requestHandler, null);
    }

    /**
     * listener loop
     */
    @Override
    public void run() {
        boolean error = false;
        Object request;
        Serializable response;

        NUM_WORKERS += 1;

        socket = CONTEXT.createSocket(ZMQ.ROUTER);
        socket.bind(endpoint);
        socket.setSendBufferSize(100000);
        socket.setHWM(100000);
        socket.setReceiveTimeOut(10);
        socket.setLinger(50);

        // TODO: use logging
        System.out.println("i am serving on endpoint: " + endpoint);

        try {
            running = true;
            while (!stopping) {
                byte[] clientId = socket.recv();
                byte[] serializedRequest = socket.recv();
                if (serializedRequest == null) {
                    // error or shutdown
                    System.out.println("received" + ", " + running + ", "
                            + stopping);
                    continue;
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
        waitForShutdown();
    }

    /**
     * clean up after shutdown
     */
    protected void cleanUp() {
        NUM_WORKERS -= 1;

        if (NUM_WORKERS == 0) {
            // TODO log: clearing context
            CONTEXT.destroy();
            // TODO log: context cleared
        } else {
            CONTEXT.destroySocket(socket);
            // TODO log: NUM_WORKERS workers left
        }
    }
}
