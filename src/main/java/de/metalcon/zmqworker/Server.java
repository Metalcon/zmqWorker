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

    /**
     * create basic server for backend component<br>
     * registers a shutdown hook calling <i>stop</i>
     * 
     * @param config
     *            ZMQ configuration object
     */
    public Server(
            ZmqConfig config) {
        this.config = config;

        // initialize ZMQ context
        context = initZmqContext(config.getNumIOThreads());

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    close();
                } catch (Exception e) {
                    // ship sinking
                }
            }
        });
    }

    /**
     * @return ZMQ configuration object
     */
    public ZmqConfig getConfig() {
        return config;
    }

    /**
     * start ZMQ communication
     * 
     * @param requestHandler
     *            handler handling request objects of server request type
     *            specified
     * @return true - if worker was started<br>
     *         false - if worker was started before and not stopped yet
     * @throws IllegalStateException
     *             if worker could not be started
     */
    public boolean start(RequestHandler<T, Response> requestHandler) {
        if (worker == null) {
            worker =
                    new ZmqWorker<T, Response>(context, config.getEndpoint(),
                            requestHandler);
            if (!worker.isAlive()) {
                throw new IllegalStateException("failed to start worker");
            }
            return true;
        }
        return false;
    }

    /**
     * stop ZMQ worker and close open context
     */
    public void close() {
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
