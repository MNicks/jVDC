package de.nikxs.digitalstrom.vdc.server.transport;

import de.nikxs.digitalstrom.vdc.server.util.UnsignedSwitch;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * VdcMessage protocol header
 * Protocol header length of 36 bytes, the details are as follows:
 *
 * <pre>
 *       Byte/     0       |       1       |       2       |       3       |
 *          /              |               |               |               |
 *         |0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|
 *         +---------------+---------------+---------------+---------------+
 *        0| id                            | flags                         |
 *         +---------------+---------------+---------------+---------------+
 *        4| log id                                                        |
 *         +---------------+---------------+---------------+---------------+
 *        8| provider                                                      |
 *         +                                                               +
 *       12|                                                               |
 *         +                                                               +
 *       16|                                                               |
 *         +                                                               +
 *       20|                                                               |
 *         +---------------+---------------+---------------+---------------+
 *       24| magic number                                                  |
 *         +---------------+---------------+---------------+---------------+
 *       28| method id                                                     |
 *         +---------------+---------------+---------------+---------------+
 *       32| body length                                                   |
 *         +---------------+---------------+---------------+---------------+
 *         Total 36 bytes
 * </pre>
 *
 * @author Markus Nicks
 */
public class VdcHeader implements Header {

    /**
     * VdcHeader size [bytes]
     */
    public static final int VDC_HEAD_LEN = 2;


    /**
     * unsigned int(4)
     * The total byte length of data after VdcHead
     */
    protected int bodyLen;

    /**
     * Turn into byte stream
     * Use small tail for conversion
     *
     * @return
     * @throws RuntimeException
     */
    @Override
    public byte[] toBytes() throws RuntimeException {
        ByteBuffer bb = ByteBuffer.allocate(VDC_HEAD_LEN);
        bb.order(ByteOrder.BIG_ENDIAN);
        try {
            bb.putShort(UnsignedSwitch.intToUshort(bodyLen));
        } catch (Exception e) {
            throw new RuntimeException("VdcHeader to byte[] failed", e);
        }

        return bb.array();
    }

    /**
     * Creates a new instance of NsHead.
     *
     * @param input
     */
    public VdcHeader(byte[] input) {
        wrap(input);
    }

    public VdcHeader(int bodyLength) {
        this.bodyLen = bodyLength;
    }

//    /**
//     * Creates a new instance of NsHead.
//     */
//    public VdcHeader() {
//
//    }

    /**
     * Restore the VdcHeader from the byte stream
     *
     * @param input
     */
    @Override
    public void wrap(byte[] input) {
        ByteBuffer bb = ByteBuffer.allocate(VDC_HEAD_LEN);
        //bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(input);
        bb.flip();
        bodyLen = UnsignedSwitch.uShortToInt(bb.getShort());
    }

    @Override
    public long getBodyLen() {
        return bodyLen;
    }

//    @Override
//    public void setBodyLen(int bodyLen) {
//        this.bodyLen = bodyLen;
//    }

//    @Override
//    public int getFixedHeaderLen() {
//        return VDC_HEAD_LEN;
//    }

}
