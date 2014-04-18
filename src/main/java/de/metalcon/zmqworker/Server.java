package de.metalcon.zmqworker;

import net.hh.request_dispatcher.RequestHandler;
import net.hh.request_dispatcher.ZmqWorkerProxy;

import org.apache.log4j.Logger;

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
public abstract class Server<T extends Request > implements AutoCloseable {

    /**
     * server log
     */
    protected static Logger LOG = Logger.getLogger("backend server");

    /**
     * ZMQ worker proxy handling communication
     */
    private ZmqWorkerProxy proxy;

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

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    LOG.debug("shutting down...");
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
        if (proxy == null) {
            proxy = new ZmqWorkerProxy(config.getEndpoint());
            proxy.add(1, requestHandler);
            proxy.startWorkers();
            LOG.info("listening @ " + config.getEndpoint());
            return true;
        }
        return false;
    }

    /**
     * stop ZMQ worker and close open context
     */
    @Override
    public void close() {
        if (proxy != null) {
            proxy.shutdown();
            proxy = null;
        }
        LOG.info("shutted down");
    }

}
