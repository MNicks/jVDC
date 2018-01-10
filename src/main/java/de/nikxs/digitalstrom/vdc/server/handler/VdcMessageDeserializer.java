package de.nikxs.digitalstrom.vdc.server.handler;

import de.nikxs.digitalstrom.vdc.server.transport.DSMessages;
import de.nikxs.digitalstrom.vdc.server.transport.VdcHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Deserialization handler
 */
@Slf4j
public class VdcMessageDeserializer extends ByteToMessageDecoder {

    /**
     * @see io.netty.handler.codec.ByteToMessageDecoder#decode(io.netty.channel.ChannelHandlerContext,
     *      io.netty.buffer.ByteBuf, java.util.List)
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        // Solve the problem of half a pack, at this moment Nshead has not received the whole,
        // the byte stream kept in channel does not deal with
        if (in.readableBytes() < VdcHeader.VDC_HEAD_LEN) {
            return;
        }

        in.markReaderIndex();

        byte[] bytes = new byte[VdcHeader.VDC_HEAD_LEN];
        in.readBytes(bytes, 0, VdcHeader.VDC_HEAD_LEN);

        VdcHeader header = new VdcHeader(bytes);

        // Solve the problem of half a pack, the body has not received all at this moment,
        // the byte stream stored in the channel is not processed, reset readerIndex
        if (in.readableBytes() < (int) header.getBodyLen()) {
            in.resetReaderIndex();
            return;
        }

        // At this point received enough of a package to begin processing
        in.markReaderIndex();

        byte[] body = new byte[(int) header.getBodyLen()];
        in.readBytes(body, 0, (int) header.getBodyLen());
        out.add(new DSMessages(body));

//        DSMessages message = DSMessages.of(header).setData(body);
//        if (message != null) {
//            out.add(new DSMessages(body));
//        }
    }
}
