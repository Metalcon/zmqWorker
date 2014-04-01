package de.metalcon.zmqworker;

import net.hh.request_dispatcher.server.RequestHandler;
import net.hh.request_dispatcher.server.ZmqWorker;

import org.zeromq.ZMQ;

import de.metalcon.api.requests.Request;
import de.metalcon.api.responses.Response;

/**
 * basic server class for backend components
 * 
 * @author sebschlicht
 * 
 * @param <T>
 *            more specific request type
 */
public abstract class Server<T extends Request > {

    /**
     * ZMQ context the worker lives in
     */
    private ZMQ.Context context;

    /**
     * ZMQ worker handling communication
     */
    private ZmqWorker<T, Response> worker;

    /**
     * ZMQ configuration object
     */
    private ZmqConfig config;

    public Server(
            ZmqConfig config,
            RequestHandler<T, Response> requestHandler) {
        this.config = config;

        // initialize ZMQ context
        context = initZmqContext(config.getNumIOThreads());

        // start ZMQ communication
        worker =
                new ZmqWorker<T, Response>(context, config.getEndpoint(),
                        requestHandler);
        if (!worker.isAlive()) {
            throw new IllegalStateException("failed to start worker");
        }
    }

    /**
     * @return ZMQ configuration object
     */
    public ZmqConfig getConfig() {
        return config;
    }

    /**
     * stop ZMQ worker and close open context
     */
    public void stop() {
        if (worker != null) {
            worker.close();
            worker = null;
        }
        if (context != null) {
            context.close();
            context = null;
        }
    }

    protected static ZMQ.Context initZmqContext(int numIOThreads) {
        System.out.println("ZMQ IO threads: " + numIOThreads);
        return ZMQ.context(numIOThreads);
    }

}
