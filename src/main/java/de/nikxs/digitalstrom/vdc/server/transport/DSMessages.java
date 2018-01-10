package de.nikxs.digitalstrom.vdc.server.transport;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Use Protobuf serial protocol to build the new VO class, only used in the host and, will not really be used
 * for serialization
 * <p>
 * A transport package contains two parts. The Header & Body. The current header using <code>VdcHeader</code> the
 * body using Protobuf protocol serialized bytecode
 * <P>
 *
 * <pre>
 *     Byte/       0       |       1       |
 *         /               |               |
 *         |0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|
 *         +---------------+---------------+
 *        0/ HEADER                        /
 *         +---------------+---------------+
 *         / serialized protobuf message   /
 *         / (note length in the Header    /
 *         / defines body field)           /
 *         +---------------+---------------+
 * </pre>
 *
 */
@Accessors(fluent=true) @Getter
public class DSMessages {

    /* The TCP stream consists of a 2-byte header containing the message length (16 bits, in network
     * byte order, maximum accepted length is 16384 bytes) followed by the protocol buffer message
     */
    public static final int MAX_DATA_SIZE = 16384; //max size of a vDC message in bytes

//    /**
//     * Service id, general client need to develop the id, the host can use this id route to a method call.<br/>
//     * Is actually the <tt>methodId</tt> in the {@link NsHead} header for passing on the host and the client
//     */
//    private int serviceId;

//    /**
//     * Service context logId, used for tracing. <br/>
//     * Actually, {@link VdcHeader} header <tt>logId</tt> for delivery on the host and client
//     */
//    @Getter private long logId;

//    /**
//     * Some do not related to business logic errorCode handled by the framework of the label,
//     * send the request, please do not set the value
//     */
//    private ErrorCode errorCode;

    /**
     * Transferred through protobuf serialized bytecode
     */
    @Getter private final byte[] data;

    public DSMessages(byte[] data) {
        this.data = data;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
//        sb.append("PbrpcMsg[logId=");
//        sb.append(logId);
//        sb.append(", serviceId=");
//        sb.append(serviceId);
//        sb.append(", provider=");
//        sb.append(provider);
        sb.append(", dataLength=");
        sb.append((data == null) ? 0 : data.length);
//        if (errorCode != null) {
//            sb.append(", errorCode=");
//            sb.append(errorCode);
//        }
        sb.append("]");
        return sb.toString();
    }

//    /**
//     * 根据NsHead构造
//     *
//     * @param header
//     * @return
//     */
//    public static DSMessages of(VdcHeader header) {
//        DSMessages ret = new DSMessages();
//        return ret;
//    }
//
//    /**
//     * Copy of simple information copy, do not copy the byte code
//     *
//     * @param msg
//     * @return
//     */
//    public static DSMessages copyLiteOf(DSMessages msg) {
//        DSMessages ret = new DSMessages();
//        return ret;
//    }

//    public ErrorCode getErrorCode() {
//        return errorCode;
//    }
//
//    public DSMessages setErrorCode(ErrorCode errorCode) {
//        this.errorCode = errorCode;
//        return this;
//    }

//    public int getServiceId() {
//        return serviceId;
//    }
//
//    public DSMessages setServiceId(int serviceId) {
//        this.serviceId = serviceId;
//        return this;
//    }


//    public DSMessages setData(byte[] data) {
//        this.data = data;
//        return this;
//    }


//    public DSMessages setLogId(long logId) {
//        this.logId = logId;
//        return this;
//    }
}
