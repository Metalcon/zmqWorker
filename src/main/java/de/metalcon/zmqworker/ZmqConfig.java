package de.metalcon.zmqworker;

/**
 * minimum configuration for services using ZMQ
 * 
 * @author sebschlicht
 * 
 */
public interface ZmqConfig {

    /**
     * @return number of IO threads to use
     */
    int getNumIOThreads();

    /**
     * @return endpoint to listen on
     */
    String getEndpoint();

}
