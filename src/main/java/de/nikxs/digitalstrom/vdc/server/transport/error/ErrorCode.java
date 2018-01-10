package de.nikxs.digitalstrom.vdc.server.transport.error;


/**
 * Common error message enumeration, used to populate <tt>flags</tt> usigned short (4) in NsHead
 * for error message exchange between host and client.<br/>
 * Here to note the general situation I hope the wrong return information, including errCode, returnCode maintained
 * by the business logic, here is the exception for the Pbprc framework, if the business logic does not handle their
 * own anomalies, then the actual client is no way to know the details, Only a general unknown exception to inform
 * the client
 */
public enum ErrorCode {

    SERVICE_NOT_FOUND(0x7f, "Service not found "),
    PROTOBUF_CODEC_ERROR(0x7e,"Protobuf codec failed "),
    INVOCATION_TARGET_EXCEPTION(0x7d, "Invocation method on target bean failed "),
    UNEXPECTED_ERROR(0x7c, "Unexpected error occurred which should not happen "),
    COMMUNICATION_ERROR(0x7b,"Communication error occurred ");

    /**
     * error code
     */
    private int value = 0;

    /**
     * wrong information
     */
    private String message = "";

    /**
     * Creates a new instance of ErrorCode.
     *
     * @param value
     * @param message
     */
    private ErrorCode(int value, String message) {
        this.value = value;
        this.message = message;
    }

    /**
     * Error enumeration returned based on error code
     *
     * @param errorCode
     * @return
     */
    public static ErrorCode get(int errorCode) {
        if (SERVICE_NOT_FOUND.getValue() == errorCode) {
            return SERVICE_NOT_FOUND;
        } else if (PROTOBUF_CODEC_ERROR.getValue() == errorCode) {
            return PROTOBUF_CODEC_ERROR;
        } else if (INVOCATION_TARGET_EXCEPTION.getValue() == errorCode) {
            return INVOCATION_TARGET_EXCEPTION;
        } else if (UNEXPECTED_ERROR.getValue() == errorCode) {
            return UNEXPECTED_ERROR;
        } else if (COMMUNICATION_ERROR.getValue() == errorCode) {
            return COMMUNICATION_ERROR;
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }
}
