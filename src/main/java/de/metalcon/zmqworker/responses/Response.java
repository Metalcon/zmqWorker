package de.metalcon.zmqworker.responses;

import java.io.Serializable;

/**
 * basic server response
 * 
 * @author sebschlicht
 * 
 */
public abstract class Response implements Serializable {

    private static final long serialVersionUID = 1194674364628069538L;

    /**
     * status message
     */
    private String statusMessage;

    /**
     * create basic server response
     * 
     * @param statusMessage
     *            status message<br>
     * @see StatusMessages
     */
    public Response(
            String statusMessage) {
    }

    /**
     * @return status message
     * @see Response
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * predefined status messages
     * 
     * @author sebschlicht
     * 
     */
    public static class StatusMessages {

        /**
         * request succeeded
         */
        public static final String SUCCESS = "success";

        /**
         * request failed
         */
        public static final String ERROR = "error";

    }

}
