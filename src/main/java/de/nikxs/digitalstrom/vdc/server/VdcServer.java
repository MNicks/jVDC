package de.nikxs.digitalstrom.vdc.server;

import de.nikxs.digitalstrom.vdc.VdcHost;
import de.nikxs.digitalstrom.vdc.config.VdcProperties;
import de.nikxs.digitalstrom.vdc.server.handler.ServerChannelIdleHandler;
import de.nikxs.digitalstrom.vdc.server.handler.VdcMessageDeserializer;
import de.nikxs.digitalstrom.vdc.server.handler.VdcMessageHandler;
import de.nikxs.digitalstrom.vdc.server.handler.VdcMessageSerializer;
import de.nikxs.digitalstrom.vdc.server.transport.VdcHeader;
import de.nikxs.digitalstrom.vdc.server.util.ByteUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.SocketUtils;
import vdcapi.Messages;


@Slf4j
@Component
public class VdcServer {

    /**
     * Parameter is used by {@link IdleStateHandler} to check whether the channel
     * has not been written or read for a long time, and shut down to save host resources.<br/>
     *      
     * Here...
     * <p>
     * <pre>
     * int readerIdleTimeSeconds,
     * int writerIdleTimeSeconds,
     * int allIdleTimeSeconds
     * </pre>
     * <p>
     * unit = seconds
     */
    private static final int IDLE_CHANNEL_TIMEOUT = 3600;

    /**
     * port vDC server should bind to for vdSM communication.
     * [default] 0 -- if not set in properties file which leads to an auto assignment of an available port
     */
    @Getter
    @Value("${vdc.server.port:0}")
    private int port;

    /**
     * Netty start object
     */
    private ServerBootstrap bootstrap;

    /**
     * Thread, operating clients request
     */
    private EventLoopGroup acceptorGroup;

    /**
     * Thread running the application logic
     */
    private EventLoopGroup handlerGroup;

    /**
     * Holder for the incoming client (vdSM) connection;
     */
    private ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Autowired
    public VdcServer(VdcProperties config) {
        log.info("Initializing vDC Server...");
        bootstrap = new ServerBootstrap();
        acceptorGroup = new NioEventLoopGroup();  // used to accept new sessions
        handlerGroup = new NioEventLoopGroup();   // handle existing connections

        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.SO_BACKLOG, config.getServer().getSoBacklog());

        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, config.getServer().isSoKeepalive());
        bootstrap.childOption(ChannelOption.TCP_NODELAY, config.getServer().isTcpNodelay());
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childOption(ChannelOption.SO_LINGER, config.getServer().getSoLinger());
        bootstrap.childOption(ChannelOption.SO_RCVBUF, config.getServer().getSoRcvbuf());
        bootstrap.childOption(ChannelOption.SO_SNDBUF, config.getServer().getSoSndbuf());
    }

    /**
     * initializing and starting vDC Server
     */
    public void start(VdcHost vDCHost) throws InterruptedException {

        try {
            log.info("Starting vDC Server...");
            if (port == 0) {
                this.port = SocketUtils.findAvailableTcpPort(1025, 65535);
            }

            VdcMessageHandler messageHandler = new VdcMessageHandler(vDCHost);
            ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(
                            "idlestate",
                            new IdleStateHandler(IDLE_CHANNEL_TIMEOUT, IDLE_CHANNEL_TIMEOUT,
                                    IDLE_CHANNEL_TIMEOUT));
                    ch.pipeline().addLast("idle", new ServerChannelIdleHandler());
                    ch.pipeline().addLast("incoming", new VdcMessageDeserializer());
                    ch.pipeline().addLast("coreHandler", messageHandler);
                    ch.pipeline().addLast("outgoing", new VdcMessageSerializer());

                    //each incoming client (vdSM) connection (channel) will be collected and used for
                    //further outbound (vDC Host --> vdSM) communication.
                    ch.pipeline().addLast("grouper", new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {

                            Channel channel = ctx.channel();
                            channel.pipeline().addLast(new PbrpcClientHandler());
                            channel.pipeline().addLast(new VdcMessageSerializer());
                            channel.pipeline().addLast(new PbrpcMessageDeserializer());

                            channel.pipeline().addLast("yourHandlerName", new SimpleChannelInboundHandler() {
                                public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
                                    // remove handler after receiving response
                                    ctx.getPipeline().remove(this);
                                    // do logic
                                }
                            });
                            channel.pipeline().addLast("discoveryServer", new SimpleChannelUpstreamHandler() {

                                @Override
                                public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
                                    try {
                                        String response = getStringMessage(e);
                                        if (response == null)
                                            return;
                                        String[] resp = response.split(":");
                                        if (resp.length == 2) {
                                            String host = resp[0];
                                            InetAddress.getByName(host);
                                            int port = Integer.parseInt(resp[1]);
                                            if (!hosts.contains(response)) {
                                                hosts.add(response);
                                                for (DiscoveryListener listener : listeners) {
                                                    listener.newHost(name, response);
                                                }
                                            }

                                        }
                                    } catch (Exception ex) {
                                        Constants.ahessianLogger.warn("", ex);
                                    }
                                }
                            });

                            allChannels.add(channel);
                            super.channelActive(ctx);
                        }
                    });
                }
            };
            bootstrap.group(acceptorGroup, handlerGroup).childHandler(initializer);
            bootstrap.bind(this.port).sync().channel();
        } catch (InterruptedException e) {
            log.error("Starting VdcServer failed" + e.getMessage(), e);
            throw e;
        }
        log.info("VdcServer started [Port:{}]", this.port);
    }

    /**
     * stop vDC Server
     */
    public void stop() {
        log.info("Stopping vDC Server");
        if (acceptorGroup != null) {
            acceptorGroup.shutdownGracefully();
        }
        if (handlerGroup != null) {
            handlerGroup.shutdownGracefully();
        }
    }

    public void send(Messages.Message message) {
        if(isConnected()) {
            byte[] bodyBytes = ByteUtil.getNonEmptyBytes(message.toByteArray());

            VdcHeader header = bodyBytes != null ? new VdcHeader(bodyBytes.length) : new VdcHeader(0);
            byte[] headerBytes = header.toBytes();

            ByteBuf encoded = Unpooled.copiedBuffer(headerBytes, bodyBytes);
            log.info("Send total byte size=" + (headerBytes.length + bodyBytes.length) + ", body size=" + bodyBytes.length);

            final ChannelGroupFuture cf = allChannels.writeAndFlush(encoded);
            cf.addListener((ChannelGroupFutureListener) cf1 -> {
                cf1.
                if(cf1.isSuccess()) {
                    System.out.println("CFListener: SUCCESS! YEAH! HELL! YEAH!");
                } else {
                    System.out.println("CFListener: failure! FAILure! FAILURE!");
                    System.out.println(cf1.cause());
                }
            });
            if (!cf.isSuccess()) {
                System.out.println("Send failed: " + cf.cause());
            }

            cf.get();
        }
    }

    public boolean isConnected() {
        return (channel != null) && channel.isWritable();
    }
}
