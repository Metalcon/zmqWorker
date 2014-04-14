package de.metalcon.zmqworker;

/**
 * minimum configuration for services using ZMQ
 * 
 * @author sebschlicht
 * 
 */
public interface ZmqConfig {

    /**
     * @return endpoint to listen on
     */
    String getEndpoint();

}
