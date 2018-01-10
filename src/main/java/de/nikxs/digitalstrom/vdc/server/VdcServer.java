package de.nikxs.digitalstrom.vdc.server;

import de.nikxs.digitalstrom.vdc.VdcHost;
import de.nikxs.digitalstrom.vdc.config.VdcProperties;
import de.nikxs.digitalstrom.vdc.server.handler.ServerChannelIdleHandler;
import de.nikxs.digitalstrom.vdc.server.handler.VdcMessageDeserializer;
import de.nikxs.digitalstrom.vdc.server.handler.VdcMessageHandler;
import de.nikxs.digitalstrom.vdc.server.handler.VdcMessageSerializer;
import de.nikxs.digitalstrom.vdc.server.transport.DSMessages;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.SocketUtils;
import vdcapi.Messages;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;


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
     * Channel used by this host to communicate with vdSM
     */
    private Channel channel;

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
                }
            };
            bootstrap.group(acceptorGroup, handlerGroup).childHandler(initializer);
            channel = bootstrap.bind(this.port).sync().channel();
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
            channel.writeAndFlush(new DSMessages(message.toByteArray()));
        }
    }

    public boolean isConnected() {
        return (channel != null && channel.isWritable()) ? true : false;
    }
}
