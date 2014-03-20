package de.metalcon.zmqworker;

import de.metalcon.api.responses.Response;

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
     *         <b>WARNING</b>: worker will be in an invalid state if you return
     *         <b>null</b><br>
     *         if you can not handle the request return usage error response
     *         instead
     */
    Response handleRequest(Object request);

}
