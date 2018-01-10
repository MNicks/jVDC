package de.nikxs.digitalstrom.vdc.server.util;

/**
 * Signed - unsigned conversion tool
 */
public class UnsignedSwitch {
    /**
     * Unsigned int to long
     *
     * @param x
     * @return
     */
    public static long uintToLong(int x) {
        return x & 0xffffffffL;
    }

    /**
     * Unsigned short to int
     *
     * @param x
     * @return
     */
    public static int uShortToInt(short x) {
        return (int) (x & 0xffff);
    }

    /**
     * long to unsigned int
     *
     * @param x
     * @return
     */
    public static int longToUint(long x) {
        return (int) (x & 0xffffffff);
    }

    /**
     * int to unsigned short
     *
     * @param x
     * @return
     */
    public static short intToUshort(int x) {
        return (short) (x & 0xffff);
    }
}
