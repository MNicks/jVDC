package de.nikxs.digitalstrom.vdc.server.handler;

import de.nikxs.digitalstrom.vdc.VdcHost;
import de.nikxs.digitalstrom.vdc.server.transport.DSMessages;
import de.nikxs.digitalstrom.vdc.server.transport.error.ErrorCode;
import de.nikxs.digitalstrom.vdc.server.codec.Codec;
import de.nikxs.digitalstrom.vdc.server.codec.ProtobufCodec;
import de.nikxs.digitalstrom.vdc.server.util.Preconditions;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.CodecException;
import lombok.extern.slf4j.Slf4j;
import vdcapi.Messages;

/**
 * Processing handler, internal routing to call the specified service bean and returning the result
 */
@Slf4j
public class VdcMessageHandler extends SimpleChannelInboundHandler<DSMessages> {

    /**
     * Configurable, the default use of protobuf to do body serialization
     */
    private Codec codec = new ProtobufCodec();
    private VdcHost host;

    /**
     * Creates a new instance of VdcMessageHandler.
     */
    public VdcMessageHandler(VdcHost host) {
        this.host = host;
    }

    /**
     * @see SimpleChannelInboundHandler#channelRead0(ChannelHandlerContext,
     *      Object)
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, DSMessages in) throws Exception {
        Preconditions
                .checkArgument(in != null, "vDC msg is null which should never happen");
        try {
//            if (DSMessages.getErrorCode() != null) {
//                ctx.channel().writeAndFlush(DSMessages.copyLiteOf(DSMessages)); // ONLY COMMUNICATIN ERROR HERE FIXME
//                return;
//            }

            Messages.Message message = (Messages.Message) codec.decode(Messages.Message.class, in.data());
            Messages.Message response = null;
            // incoming messages (vdSM - vDC) messages needs to be delegated to vDC host for further processing
            switch (message.getType()) {

                case VDSM_REQUEST_HELLO:
                    log.debug("vdSM ==> request hello() ...");
                    response = host.processHello(message);
                    break;

                case VDSM_SEND_PING:
                    log.debug("vdSM ==> send ping() ...");
                    response = host.processPing(message);
                    break;

                case VDSM_SEND_BYE:
                    log.debug("vdSM ==> send bye() ...");
                    response = host.processBye(message);
                    break;

                case VDSM_SEND_REMOVE:
                    log.debug("vdSM ==> send remove() ...");
                    response = host.processRemove(message);
                    break;

                case GENERIC_RESPONSE:
                    log.debug("vdSM ==> send genericResponse() ...");
                    host.processGenericResponse(message);
                    break;

                case VDSM_REQUEST_GET_PROPERTY:
                    log.debug("vdSM ==> send getProperty() ...");
                    break;

                case VDSM_REQUEST_SET_PROPERTY:
                    log.debug("vdSM ==> send setProperty() ...");
                    break;

                case VDSM_REQUEST_GENERIC_REQUEST:
                    log.warn("unsupported messageType {}", message.getType().toString());
                    break;

                default:
                    log.warn("unsupported messageType {}", message.getType().toString());
                    break;

                    //                case VDC_SEND_ANNOUNCE_DEVICE: doNothing();
//                case VDC_SEND_VANISH: doNothing();
//                case VDC_SEND_PUSH_NOTIFICATION: doNothing();
//                case VDC_SEND_ANNOUNCE_VDC: doNothing();
//                case VDC_SEND_IDENTIFY: doNothing();

//                case VDSM_NOTIFICATION_CALL_SCENE: doNothing();
//                case VDSM_NOTIFICATION_SAVE_SCENE: doNothing();
//                case VDSM_NOTIFICATION_UNDO_SCENE: doNothing();
//                case VDSM_NOTIFICATION_SET_LOCAL_PRIO: doNothing();
//                case VDSM_NOTIFICATION_CALL_MIN_SCENE: doNothing();
//                case VDSM_NOTIFICATION_IDENTIFY: doNothing();
//                case VDSM_NOTIFICATION_SET_CONTROL_VALUE: doNothing();
//                case VDSM_NOTIFICATION_DIM_CHANNEL: doNothing();
//                case VDSM_NOTIFICATION_SET_OUTPUT_CHANNEL_VALUE: doNothing();
            }

            if (response != null) {
                DSMessages out = new DSMessages(response.toByteArray());
                ctx.channel().writeAndFlush(out);
            }


            // LOG.info(servDesc + " exec using " + (System.currentTimeMillis() - start) + "ms");
//        } catch (ServiceNotFoundException e) {
//            log.error(ErrorCode.SERVICE_NOT_FOUND.getMessage() + e.getMessage(), e);
//            ctx.channel().writeAndFlush(
//                    DSMessages.copyLiteOf(DSMessages).setErrorCode(ErrorCode.SERVICE_NOT_FOUND));
        } catch (CodecException e) {
            log.error(ErrorCode.PROTOBUF_CODEC_ERROR.getMessage() + e.getMessage(), e);
            ctx.channel().writeAndFlush(new DSMessages(null));
                    //DSMessages.copyLiteOf(in.setErrorCode(ErrorCode.PROTOBUF_CODEC_ERROR));
//        } catch (InvocationTargetException e) {
//            log.error(ErrorCode.INVOCATION_TARGET_EXCEPTION.getMessage() + e.getMessage(), e);
//            ctx.channel().writeAndFlush(
//                    DSMessages.copyLiteOf(DSMessages).setErrorCode(
//                            ErrorCode.INVOCATION_TARGET_EXCEPTION));
        } catch (Exception e) {
            log.error(ErrorCode.UNEXPECTED_ERROR.getMessage() + e.getMessage(), e);
            ctx.channel().writeAndFlush(new DSMessages(null));
                    //DSMessages.copyLiteOf(DSMessages).setErrorCode(ErrorCode.UNEXPECTED_ERROR));
        }
    }

    /**
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelReadComplete(ChannelHandlerContext)
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    }

    /**
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelActive(ChannelHandlerContext)
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    }

    /**
     * @see io.netty.channel.ChannelInboundHandlerAdapter#exceptionCaught(ChannelHandlerContext,
     *      Throwable)
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getCause().getMessage(), cause.getCause());
        // ctx.channel().writeAndFlush(retMsg);
        ctx.fireChannelRead(new DSMessages(new byte[0])); // FIXME is this handling of communication anomalies OK？
    }
}
