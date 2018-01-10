package de.nikxs.digitalstrom.vdc.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class ByteUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ByteUtil.class);


    /**
     * The default character encoding
     */
    private static final String STR_ENCODE = "UTF-8";

    /**
     * The string is converted to UTF-8 bytecode, the length of 16 bytes
     *
     * @param input
     * @param length
     * @return
     */
    public static byte[] convertStringToBytes(String input, int length) {
        byte[] tmp = convertStringToBytes(input);
        byte[] bytes = new byte[length];
        System.arraycopy(tmp, 0, bytes, 0, tmp.length);
        return bytes;
    }

    /**
     * Convert a string to UTF-8 bytecode
     *
     * @param input
     * @return byte[]
     */
    public static byte[] convertStringToBytes(String input) {
        byte[] result = null;
        if (input != null) {
            try {
                result = input.getBytes(STR_ENCODE);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Bytecode to string
     *
     * @param input
     * @return
     */
    public static String convertBytesToString(byte[] input) {
        String result = null;
        if (input != null) {
            try {
                result = new String(input, STR_ENCODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Get non-null bytecode
     *
     * @param data
     * @return
     */
    public static byte[] getNonEmptyBytes(byte[] data) {
        if (data == null) {
            return new byte[0];
        } else {
            return data;
        }
    }

    /**
     * read inputstream to a byte array
     *
     * @param body
     * @param in
     * @return
     * @throws IOException
     */
    public static int read(byte[] body, InputStream in) throws IOException {
        byte[] buffer = new byte[1024 * 32];
        int readlength = 0;
        int offset = 0;
        int bytesRead = -1;
        try {
            while (offset < body.length) {
                if ((body.length - offset) > buffer.length) {
                    readlength = buffer.length;
                } else {
                    readlength = body.length - offset;
                }
                bytesRead = in.read(buffer, 0, readlength);

                if (bytesRead != -1) {
                    System.arraycopy(buffer, 0, body, offset, bytesRead);
                    offset += bytesRead;
                } else {
                    break;
                }

            }
        } catch (Exception e) {
            LOG.warn("bytesRead:" + bytesRead + "; offset:" + offset + "; BODT_length:"
                    + body.length, e);
            throw new IOException(e.getMessage());

        } finally {
            readlength = offset;
        }

        return readlength;
    }
}
