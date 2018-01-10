package de.nikxs.digitalstrom.vdc.server.codec;

import io.netty.handler.codec.CodecException;

public interface Codec {

    /**
     * Deserialization
     *
     * @param clazz Deserialized class definition
     * @param bytes serialized object
     * @return Deserialized object
     *
     * @throws CodecException
     */
    Object decode(Class<?> clazz, byte[] bytes) throws CodecException;

    /**
     * Deserialization
     *
     * @param clazz to be serialized class definition
     * @param object The object to be serialized
     * @return Byte code

     * @throws CodecException
     */
    byte[] encode(Class<?> clazz, Object object) throws CodecException;
}
