package de.nikxs.digitalstrom.vdc.config;

import de.nikxs.digitalstrom.vdc.util.DSUID;
import io.netty.channel.ChannelOption;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Component
@ConfigurationProperties(prefix="vdc")
@Validated
@ToString
@Getter
@Setter
public class VdcProperties {

    private String name;

    private String springVersion;

    /**
     * configuration for vDC Server (internally based on Netty) used for vdSM communication
     */
    @Valid
    @NotNull
    private final Server server = new Server();

    /**
     * configuration for vDC Host
     */
    @Valid
    @NotNull
    private final Host host = new Host();

    @ToString
    @Getter
    @Setter
    public static class Server {

        /**
         * port <code>VdcServer</code> should use for communication with a vdSM
         * (default: 0 --> find automatically a  in between port range of 1025-65535)
         */
        @Min(1025)
        @Max(65535)
        private int port = 0;

        /**
         * [Netty] keep alive (default: true)
         *
         * @see ChannelOption#SO_KEEPALIVE
         */
        private boolean soKeepalive = true;

        /**
         * [Netty] tcp no delay (default: true)
         *
         * @see ChannelOption#TCP_NODELAY
         */
        private boolean tcpNodelay = true;

        /**
         * [Netty] so linger (default: 2)
         *
         * @see ChannelOption#SO_LINGER
         */
        private int soLinger = 2;

        /**
         * [Netty] so backlog (default: 128)
         *
         * @see ChannelOption#SO_BACKLOG
         */
        private int soBacklog = 128;

        /**
         * [Netty] receive buf size (default: 1024 * 64)
         *
         * @see ChannelOption#SO_RCVBUF
         */
        private int soRcvbuf = 1024 * 64;

        /**
         * [Netty] send buf size (default: 1024 * 64)
         *
         * @see ChannelOption#SO_SNDBUF
         */
        private int soSndbuf = 1024 * 64;
    }

    @ToString
    @Getter
    @Setter
    public static class Host {
        /**
         * unique dS UUID of vDC Host
         * ex.
         * xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx (136-bit --> 17 bytes)
         * 9888dd3d-b345-4109-b088-2673306d0c65
         *
         * see https://stackoverflow.com/questions/136505/searching-for-uuids-in-text-with-regex
         */
        //@Pattern(regexp = "^[0-9A-F]{8}-[0-9A-F]{4}-[4][0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}$")
        private DSUID dsuid;

        /**
         *  custom name for the vDC Host
         */
        private String name;

    }
}
