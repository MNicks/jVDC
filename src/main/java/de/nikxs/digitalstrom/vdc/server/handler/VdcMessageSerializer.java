package de.nikxs.digitalstrom.vdc.server.handler;

import de.nikxs.digitalstrom.vdc.server.transport.DSMessages;
import de.nikxs.digitalstrom.vdc.server.transport.VdcHeader;
import de.nikxs.digitalstrom.vdc.server.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Serialized Handler
 */
@Slf4j
public class VdcMessageSerializer extends MessageToMessageEncoder<DSMessages> {

    /**
     * @see MessageToMessageEncoder#encode(ChannelHandlerContext,
     *      Object, List)
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, DSMessages vdcMsg, List<Object> out) {
        byte[] bodyBytes = ByteUtil.getNonEmptyBytes(vdcMsg.data());

        VdcHeader header = constructHeader(bodyBytes);
        byte[] headerBytes = header.toBytes();

        ByteBuf encoded = Unpooled.copiedBuffer(headerBytes, bodyBytes);
        out.add(encoded);

        log.info("Send total byte size=" + (headerBytes.length + bodyBytes.length) + ", body size=" + bodyBytes.length);
    }

    /**
     * Build VdcHeader
     *
     * @param data
     * @return VdcHeader
     */
    private VdcHeader constructHeader(byte[] data) {
        if (data != null) {
            return new VdcHeader(data.length);
        } else {
            return new VdcHeader(0);
        }
    }
}
