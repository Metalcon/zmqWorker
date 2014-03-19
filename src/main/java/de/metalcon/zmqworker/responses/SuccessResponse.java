package de.metalcon.zmqworker.responses;

/**
 * simple server response signalizing success of the request
 * 
 * @author sebschlicht
 * 
 */
public class SuccessResponse extends Response {

    private static final long serialVersionUID = 8432384339392710489L;

    /**
     * create success response
     */
    public SuccessResponse() {
        super(StatusMessages.SUCCESS);
    }

}
