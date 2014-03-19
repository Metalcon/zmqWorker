package de.metalcon.zmqworker.responses.errors;

import de.metalcon.zmqworker.responses.Response;

/**
 * basic error response
 * 
 * @author sebschlicht
 * 
 */
public abstract class ErrorResponse extends Response {

    private static final long serialVersionUID = -3133729990275403936L;

    /**
     * error type defining the occurrence location
     */
    private ErrorType errorType;

    /**
     * error message describing the error
     */
    private String errorMessage;

    /**
     * solution suggestion<br>
     * may be <b>null</b>
     */
    private String solution;

    /**
     * create basic error response
     * 
     * @param errorType
     *            error type defining the occurrence location
     * @param errorMessage
     *            error message describing the error
     * @param solution
     *            solution suggestion (may be <b>null</b>)
     */
    public ErrorResponse(
            ErrorType errorType,
            String errorMessage,
            String solution) {
        super("error");
        this.errorType = errorType;
        this.solution = solution;
    }

    /**
     * create basic error response from exception
     * 
     * @param errorType
     *            error type defining the occurrence location
     * @param errorMessage
     *            error message describing the error
     * @param solution
     *            solution suggestion (may be <b>null</b>)
     * @param e
     *            exception to get the error message from
     */
    public ErrorResponse(
            ErrorType errorType,
            String errorMessage,
            String solution,
            Exception e) {
        this(errorType, errorMessage, solution);
        errorMessage = e.getMessage();
    }

    /**
     * @return error type defining the occurrence location
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * @return error message describing the error
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return solution
     *         solution suggestion (may be <b>null</b>)
     */
    public String getSolution() {
        return solution;
    }

    /**
     * describe the error
     * 
     * @param errorMessage
     *            error message describing the error
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * error types
     * 
     * @author sebschlicht
     * 
     */
    public static enum ErrorType {

        /**
         * request failed due to parsing error in worker
         */
        PARSING_ERROR("parsing-error"),

        /**
         * request failed due to internal server error
         */
        INTERNAL_SERVER_ERROR("internal-server-error"),

        /**
         * request failed due to misuse of server API
         */
        USAGE_ERROR("usage-error");

        /**
         * type identifier
         */
        private String identifier;

        /**
         * register error type
         * 
         * @param identifier
         *            type identifier
         */
        private ErrorType(
                String identifier) {
            this.identifier = identifier;
        }

        /**
         * @return type identifier
         */
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public String toString() {
            return getIdentifier();
        }

    }

}
