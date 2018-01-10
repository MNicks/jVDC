package de.nikxs.digitalstrom.vdc.server.transport;

/**
 * Common headers for RPC communication messages belong to the header + body interaction
 *
 * @author Markus Nicks
 */
public interface Header {

//    /**
//     * Body body length
//     *
//     * @param bodyLen
//     */
//    void setBodyLen(int bodyLen);

    /**
     * Get body body length message
     *
     * @return
     */
    long getBodyLen();

    /**
     * Construct headers from bytecode
     *
     * @param input
     */
    void wrap(byte[] input);

    /**
     * Header serialized to bytecode
     *
     * @return
     * @throws RuntimeException
     */
    byte[] toBytes() throws RuntimeException;
}
