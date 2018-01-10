package de.nikxs.digitalstrom.vdc.server.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle some idle connections to close them, to prevent the host resources from being occupied
 */
public class ServerChannelIdleHandler extends ChannelDuplexHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ServerChannelIdleHandler.class);

    /**
     * Creates a new instance of erverChannelIdleHandler.
     */
    public ServerChannelIdleHandler() {

    }

    /**
     * @see io.netty.channel.ChannelInboundHandlerAdapter#userEventTriggered(io.netty.channel.ChannelHandlerContext,
     *      java.lang.Object)
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.WRITER_IDLE) {
                LOG.warn("Write idle on channel:" + ctx.channel() + " is timeout");
            } else if (e.state() == IdleState.READER_IDLE) {
                LOG.warn("Read idle on channel:" + ctx.channel() + " is timeout on "
                        + ctx.channel().remoteAddress() + ", so close it");
                // ctx.fireExceptionCaught(ReadTimeoutException.INSTANCE);
                ctx.close();
            }
        }
    }

}
