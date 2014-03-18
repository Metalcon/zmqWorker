package de.metalcon.zmqworker;

import java.io.Serializable;

/**
 * interface for request handler of backend components using ZeroMQ
 * 
 * @author sebschlicht
 * 
 */
public interface ZMQRequestHandler {

    /**
     * handle a request object
     * 
     * @param request
     *            request object
     * @return response object
     */
    Serializable handleRequest(Object request);

}
