package de.nikxs.digitalstrom.vdc.server.codec;

import com.google.protobuf.GeneratedMessageV3;
import de.nikxs.digitalstrom.vdc.server.util.Computable;
import de.nikxs.digitalstrom.vdc.server.util.ConcurrentCache;
import io.netty.handler.codec.CodecException;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * protobuf serializer, using the reflection cache <tt>method</tt> to make a call
 *
 * @author Markus Nicks
 */
public class ProtobufCodec implements Codec {

    /**
     * Protobuf Generates the name of the method decoding method in native Java code
     */
    private static final String METHOD_NAME_PARSEFROM = "parseFrom";

    /**
     * Protobuf Generates the name of the method encoding method in native Java code
     */
    private static final String METHOD_NAME_TOBYTE = "toByteArray";

    /**
     * Method cache, used by Protobuf to generate some codecs in native Java code. Caching methods include:
     *
     * <p/>
     * <ul>
     * <li><code>parseFrom(byte[] bytes)</code></li>
     * <li><code>toByteArray()</code></li>
     * </ul>
     *
     * @see ConcurrentCache
     * @see Computable
     */
    private static final Computable<String, Method> PROTOBUF_METHOD_CACHE = new ConcurrentCache<String, Method>();

    /**
     * @see Codec#decode(java.lang.Class, byte[])
     */
    @Override
    public Object decode(final Class<?> clazz, byte[] data) throws CodecException {
        try {
            if (data == null || data.length == 0) {
                return null;
            }
            Method m = PROTOBUF_METHOD_CACHE.get(clazz.getName() + METHOD_NAME_PARSEFROM,
                    new Callable<Method>() {
                        @Override
                        public Method call() throws Exception {
                            return clazz.getMethod(METHOD_NAME_PARSEFROM, byte[].class);
                        }
                    });
            GeneratedMessageV3 msg = (GeneratedMessageV3) m.invoke(clazz, data);
            return msg;
        } catch (Exception e) {
            throw new CodecException("Decode failed due to " + e.getMessage(), e);
        }
    }

    /**
     * @see Codec#encode(java.lang.Class, java.lang.Object)
     */
    @Override
    public byte[] encode(final Class<?> clazz, Object object) throws CodecException {
        try {
            Method m = PROTOBUF_METHOD_CACHE.get(clazz.getName() + METHOD_NAME_TOBYTE,
                    new Callable<Method>() {
                        @Override
                        public Method call() throws Exception {
                            return clazz.getMethod(METHOD_NAME_TOBYTE);
                        }
                    });
            byte[] data = (byte[]) m.invoke(object);
            return data;
        } catch (Exception e) {
            throw new CodecException("Encode failed due to " + e.getMessage(), e);
        }
    }
}
