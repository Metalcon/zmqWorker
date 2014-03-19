package de.metalcon.zmqworker;

import de.metalcon.zmqworker.responses.Response;

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
     * @return response object<br>
     *         <b>null</b> if the request object was unknown
     */
    Response handleRequest(Object request);

}
