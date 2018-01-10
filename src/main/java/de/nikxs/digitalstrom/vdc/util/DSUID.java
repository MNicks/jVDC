package de.nikxs.digitalstrom.vdc.util;

import lombok.EqualsAndHashCode;

import javax.xml.bind.DatatypeConverter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

/**
 * digitalStrom UID (dsUID) is unique identifier each entities /(eg. vDC, vdSD, ...) in a dS system needs to provide.
 *
 * dsUID is a 136-bit (17 byte) id derived from a 128-bit (16 byte) UUIDs (Universally Unique Identifier) as defined
 * in RFC4122 by appending a additional byte at the end of UUID
 *
 * <pre>
 *  Byte:    0 1 2 3  4 5  6 7  8 9 101112131415 16
 *  UUID:   xxxxxxxx-xxxx-Vxxx-Txxx-xxxxxxxxxxxx    (128-bit --> 16 bytes) <br/>
 *          123e4567-e89b-12d3-a456-426655440000    <br/><br/>
 *  DSUID:  xxxxxxxx-xxxx-Vxxx-Txxx-xxxxxxxxxxxx ii (136-bit --> 17 bytes) <br/>
 *          123e4567-e89b-12d3-a456-426655440000 00 <br/><br/>
 *
 * The four bits of digit <b>V</b> indicate the UUID version, and the one to three most significant bits of
 * digit <b>T</b> indicate the UUID type/variant. In the example, V is 1 and N is 'a' (10xx), meaning that the UUID is a
 * variant 1, version 1 UUID; that is, a time-based DCE/RFC 4122 UUID.
 * </pre>
 *
 *      <b>UUID Record Layout</b>
 *      <table>
 *          <tr>
 *              <th>Name</th>
 *              <th>Length (Bytes)</th>
 *              <th>Length (Hex Digits)</th>
 *              <th>Contents</th>
 *          </tr>
 *          <tr>
 *              <td>time_low</td>
 *              <td>4</td>
 *              <td>8</td>
 *              <td>integer giving the low 32 bits of the time</td>
 *          </tr>
 *          <tr>
 *              <td>time_mid</td>
 *              <td>2</td>
 *              <td>4</td>
 *              <td>integer giving the middle 16 bits of the time</td>
 *          </tr>
 *          <tr>
 *              <td>time_hi_and_version</td>
 *              <td>2</td>
 *              <td>4</td>
 *              <td>4-bit "version" in the most significant bits, followed by the high 12 bits of the time</td>
 *          </tr>
 *          <tr>
 *              <td>clock_seq_hi_and_res clock_seq_low</td>
 *              <td>2</td>
 *              <td>4</td>
 *              <td>1-3 bit "variant" in the most significant bits, followed by the 13-15 bit clock sequence</td>
 *          </tr>
 *          <tr>
 *              <td>node</td>
 *              <td>6</td>
 *              <td>12</td>
 *              <td>the 48-bit node id</td>
 *          </tr>
 *       </table>
 *
 *  @see <a href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>
 */
@EqualsAndHashCode
public final class DSUID {

    public static short SGTIN96_HEADER = 0x30; // SGTIN96 8bit header byte

    /**
     * Inner UUID of this DSUID which will be created by "generateXXX" methods
     */
    private UUID uuid;    // 128-bit (16 byte)
    private char[] dsuid; // 136-bit (17 byte)
    private Type type = Type.UUID;

    public enum NamespaceUUID {
        ENOCEAN("0ba94a7b-7c92-4dab-b8e3-5fe09e83d0f3"),
        GS1_128("8ca838d5-4c40-47cc-bafa-37ac89658962"),
        IEEE_MAC("ad019650-5330-4582-8506-62c7340b34e6"),
        VDSM("195de5c0-902f-4b71-a706-b43b80765e3d"),
        VDC("9888dd3d-b345-4109-b088-2673306d0c65");
        
        private String uuid;

        NamespaceUUID(String uuid) {
            this.uuid = uuid;
        }

        public String getUuid() {
            return uuid;
        }

        /**
         * get "plain" uuid (without dashes ("-")
         *
         * @return String representation of the NS UUID
         */
        public String getUuidPlain() {
            return uuid.replace("-", "");
        }
    }

    /**
     * Type of Id
     */
    public enum Type {
        UNDEFINED,
        /**
         * dSUID based on SGTIN96
         */
        SGTIN,
        /**
         * dSUID based on UUID
         */
        UUID
    }

    /* helper variable used for generating a time based (variant 1) UUID */
    private static final Object lock = new Object();
    private static long lastTime;
    private static long clockSequence = 0;
    private static final long hostIdentifier = getHostId();

    private DSUID() {
        this(UUID.randomUUID());
    }

    private DSUID(String dsuid) {
        this(uuidFromDSUID(dsuid));
    }

    private DSUID(UUID uuid) {
        this.uuid = uuid;
        this.dsuid = toDSUID(uuid);
    }

//    /**
//     * see {@link java.util.UUID#version() version()} method.
//     * @return  The version number of the underlying {@code UUID}
//     */
//    public int version() {
//        return this.uuid.version();
//    }

    /**
     * get the string representation of this DSUID
     */
    public String toString() {
        return new String(this.dsuid);
    }

    /**
     * Comparison will be done against internal UUID
     * @param val
     * @return
     */
    public int compareTo(DSUID val) {
        return this.uuid.compareTo(val.uuid);
    }

    /**
     * Creates a “time based UUIDv4” according variant 1 of UUID spec.
     *
     * @return A new {@code DSUID} based on UUIDv4 variant 1
     */
    public static DSUID generateV1() {

        long currentTimeMillis = System.currentTimeMillis();
        long time;

        synchronized (lock) {
            if (currentTimeMillis > lastTime) {
                lastTime = currentTimeMillis;
                clockSequence = 0;
            } else  {
                ++clockSequence;
            }
        }

        time = currentTimeMillis << 32;  // low Time
        time |= ((currentTimeMillis & 0xFFFF00000000L) >> 16); // mid Time
        time |= 0x1000 | ((currentTimeMillis >> 48) & 0x0FFF); // hi Time

        long clockSequenceHi = clockSequence;

        clockSequenceHi <<=48;

        long lsb = clockSequenceHi | hostIdentifier;

        return new DSUID(new UUID(time, lsb));
    }

    /**
     * Static factory to retrieve new namespace based UUIDv3 (MD5-hashed). This generates a reproducable
     * UUID based on given parameter.
     *
     * @param ns namespace the new uuid should base on
     * @param name name which should be considered when generating uuid
     * @return A new {@code DSUID} based on UUIDv3
     */
    public static DSUID generateV3(NamespaceUUID ns, String name) {

        byte[] nsbytes = DatatypeConverter.parseHexBinary(ns.getUuidPlain());
        byte[] namebytes = name.getBytes(Charset.forName("UTF-8"));

        // concat both byte arrays
        byte[] allBytes = new byte[nsbytes.length + namebytes.length];
        System.arraycopy(nsbytes, 0, allBytes, 0, nsbytes.length);
        System.arraycopy(namebytes, 0, allBytes, nsbytes.length, namebytes.length);

        return new DSUID(UUID.nameUUIDFromBytes(allBytes));
    }

    /**
     * Static factory to retrieve new namespace based UUIDv5 (SHA1-hashed). This generates a reproducable
     * UUID based on given parameter.
     *
     * @param ns namespace the new uuid should base on
     * @param name name which should be considered when generating uuid
     * @return A new {@code DSUID} based on UUIDv3
     */
    public static DSUID generateV5(NamespaceUUID ns, String name) {

        byte[] nsbytes = DatatypeConverter.parseHexBinary(ns.getUuidPlain());
        byte[] namebytes = name.getBytes(Charset.forName("UTF-8"));

        // concat both byte arrays
        byte[] allBytes = new byte[nsbytes.length + namebytes.length];
        System.arraycopy(nsbytes, 0, allBytes, 0, nsbytes.length);
        System.arraycopy(namebytes, 0, allBytes, nsbytes.length, namebytes.length);

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("MD5 not supported", nsae);
        }
        byte[] sha1Bytes = md.digest(allBytes);
        sha1Bytes[6]  &= 0x0f;  /* clear version        */
        sha1Bytes[6]  |= 0x50;  /* set to version 5     */
        sha1Bytes[8]  &= 0x3f;  /* clear variant        */
        sha1Bytes[8]  |= 0x90;  /* set to IETF variant  */

        long msb = 0;
        long lsb = 0;
        for (int i=0; i<8; i++)
            msb = (msb << 8) | (sha1Bytes[i] & 0xff);
        for (int i=8; i<16; i++)
            lsb = (lsb << 8) | (sha1Bytes[i] & 0xff);
        return new DSUID(new UUID(msb, lsb));
    }

    /**
     * Creates a new “random" DSUID according version 4 of UUID spec.
     *
     * @return A new {@code DSUID} based on UUID version 4
     */
    public static DSUID generateV4() {
        return new DSUID();
    }

    /**
     * Creates a {@code DSUID} from the string standard representation as
     * described in the {@link #toString} method.
     *
     * generate a DSUID from a given DSUID string representation.
     *
     * @return A new {@code DSUID} from the specified string
     */
    public static DSUID fromDSUID(String dsuid) {
        return new DSUID(dsuid);
    }

    private static UUID uuidFromDSUID(String dsuid) {
        // UUID string format
        // 0      7 8 11 1215 1619 20        31 32
        // xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx(ii) // last digit belongs to dSUID and must be ignored
        // 9888dd3d-b345-4109-b088-2673306d0c65 00
        return UUID.fromString(
                dsuid.substring(0, 8) + "-" +
                dsuid.substring(8, 12) + "-" +
                dsuid.substring(12, 16) + "-" +
                dsuid.substring(16, 20) + "-" +
                dsuid.substring(20, 32));
    }

    /**
     * converts UUID (128-bit) to DSUID (136-bit) by appending a "0" byte at
     * @param uuid
     * @return
     */
    private char[] toDSUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[17]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        String s_uuid = DatatypeConverter.printHexBinary(bb.array());
        return s_uuid.toCharArray();
    }

    /**
     * helper getting unique id considering the systems MAC address
     *
     * @return
     */
    private static final long getHostId(){
        long  macAddressAsLong = 0;
        try {
            Random random = new Random();
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            if (ni != null) {
                byte[] mac = ni.getHardwareAddress();
                random.nextBytes(mac); // we don't really want to reveal the actual MAC address

                //Converts array of unsigned bytes to an long
                if (mac != null) {
                    for (int i = 0; i < mac.length; i++) {
                        macAddressAsLong <<= 8;
                        macAddressAsLong ^= (long)mac[i] & 0xFF;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return macAddressAsLong;
    }
}
