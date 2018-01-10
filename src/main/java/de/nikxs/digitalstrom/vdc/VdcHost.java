package de.nikxs.digitalstrom.vdc;

import de.nikxs.digitalstrom.vdc.config.VdcProperties;
import de.nikxs.digitalstrom.vdc.server.Request;
import de.nikxs.digitalstrom.vdc.server.Session;
import de.nikxs.digitalstrom.vdc.server.VdcServer;
import de.nikxs.digitalstrom.vdc.util.DSUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import vdcapi.Messages;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static de.nikxs.digitalstrom.vdc.util.DsUtil.buildGenericResponse;


@Slf4j
@ToString(exclude="dSEntities")
@Component
public class VdcHost implements DsAddressable {

    private static final DSUID DEFAULT_HOST_DSUID = DSUID.fromDSUID("6123A881016010000000F2CA0DEB370700");

    private static final int SUPPORTED_API_VERSION = 3;
    private static final int RESERVED_REQUEST_ID = 0;

    private static final int TIMEOUT_ANNOUNCEMENT = 30; // timespan after an announcement is treated as failed

    /**
     * dS specific unique ID of a vDC host instance
     */
    @Getter
    private DSUID dSUID;

    /**
     * human readable name of this dS entity
     */
    @Getter
    @Setter
    @Value("${vdc.host.name:'default-vDC-Host'}")
    private String name;

    /**
     * Indicator whether vDC host is connected to a vdSM
     */
    private Session session;

    private VdcProperties config;

    private VdcServer server;

    /**
     * <code>true</code> if a connection to vdSM is established
     */
    @Getter
    private boolean connected = false;

    /**
     * List of all dS devices managed by this host
     */
    private HashMap<DSUID, DsAddressable> dSEntities = new HashMap<>();

    /**
     * Cache which holds temporary all requests to the connected vdSM till the corresponding response is received
     * and properly processed
     */
    private HashMap<Integer, Request> requestCache = new HashMap<>();

    /**
     * Multi-cast DNS implementation for service (vDC Host) registration using Apple's Bonjour/ Zeroconf/ .
     */
    private JmDNS jmDNS;

    /**
     * Creates a new instance of VdcHost
     */
    @Autowired
    public VdcHost(VdcProperties config, VdcServer server) {
        this.config = config;
        this.server = server;

        DSUID tempdsUID = config.getHost().getDsuid();
        if(tempdsUID != null) {
            this.dSUID = tempdsUID;
        } else {
            this.dSUID = DEFAULT_HOST_DSUID;
        }
        dSEntities.put(this.getDSUID(), this);
    }

//    @DependsOn("vdcServer")
//    public void collectDevices(int aCompletedCB, int aRescanFlags) {
//
//    };

    /**
     * Handle a incoming hello() request coming from vdSM.
     *
     * @param request incoming request
     * @return hello() response
     */
    public Messages.Message processHello(Messages.Message request) {

        if (!request.hasMessageId() || !request.hasVdsmRequestHello() || !request.getVdsmRequestHello().hasDSUID()) {
            log.error("hello() -- No message ID!");
            return buildGenericResponse(Messages.ResultCode.ERR_MISSING_DATA, request.getMessageId());
        }

        if (!request.getVdsmRequestHello().hasApiVersion() || request.getVdsmRequestHello().getApiVersion() > SUPPORTED_API_VERSION) {
            log.error("hello() -- Missing or incompatible API version information");
            return buildGenericResponse(Messages.ResultCode.ERR_INCOMPATIBLE_API, request.getMessageId());
        }

        if(!isConnected()) {
            session = new Session(DSUID.fromDSUID(request.getVdsmRequestHello().getDSUID()));
            log.info("hello() -- New connection to vdSM ({}) established", session.getVdSMdSUID());

            return Messages.Message.newBuilder()
                    .setType(Messages.Type.VDC_RESPONSE_HELLO)
                    .setMessageId(request.getMessageId())
                    .setVdcResponseHello(request.getVdcResponseHello().toBuilder().setDSUID(getDSUID().toString()).build())
                    .build();
        } else if (isConnected() && session.getVdSMdSUID().equals(DSUID.fromDSUID(request.getVdsmRequestHello().getDSUID()))) {
            log.info("hello() -- vdSM ({}) request renewing the connection", request.getVdsmRequestHello().getDSUID());

            //@ToDo: May be the reconnect signal should be forwarded to managed vDCs

            return Messages.Message.newBuilder()
                    .setType(Messages.Type.VDC_RESPONSE_HELLO)
                    .setMessageId(request.getMessageId())
                    .setVdcResponseHello(request.getVdcResponseHello().toBuilder().setDSUID(getDSUID().toString()).build())
                    .build();
        } else {
            log.error("hello() -- Already connected to another vdSM. Dropping this request");
            return buildGenericResponse(Messages.ResultCode.ERR_SERVICE_NOT_AVAILABLE, request.getMessageId());
        }
    }

    /**
     * Handle a incoming ping() request coming from vdSM. If the requests dSUID belongs to this host, to a registered
     * vDC or one of its managed vdSD the pong() method of the corresponding dS entity is implicitly called.
     *
     * @param request incoming request
     * @return pong() response of the respective vdSD
     */
    public Messages.Message processPing(Messages.Message request) {
        if(!request.hasVdsmSendPing() || !request.getVdsmSendPing().hasDSUID()) {
            log.warn("ping() -- Incomplete request. No payload or dSUID");
            return null;
        }

        DsAddressable entity = findEntity(DSUID.fromDSUID(request.getVdsmSendPing().getDSUID()));
        if (entity != null) {
            log.debug("ping() -- delegate request to dS entity '{}' ({})", entity.getName(), entity.getDSUID());
            return entity.pong(request);
        } else {
            log.warn("ping() -- Drop request. Unknown device ({})", request.getVdsmSendPing().getDSUID());
            return null;
        }
    }

    /**
     * Handle incoming processBye() request. If the given dSUID belongs to this {@link VdcHost} the virtual
     * vdSM session wil be terminated and all managed vDC becomes inactive
     *
     * @param request incoming request
     * @return {@link vdcapi.Messages.GenericResponse} with "OK" code if the given dSUID belongs to a known vDC. Otherwise {@code null}
     */
    public Messages.Message processBye(Messages.Message request) {
        if(!request.hasVdsmSendBye() || !request.getVdsmSendBye().hasDSUID()) {
            log.warn("bye() -- Incomplete request. No payload or dSUID");
            return null;
        }

        if (isConnected() && session.getVdSMdSUID().equals(DSUID.fromDSUID(request.getVdsmSendBye().getDSUID()))) {
            log.info("bye() -- Invalidate session to vdSM ({})", request.getVdsmSendBye().getDSUID());
            session.invalidate();

            //bye() request might also be for interest to all vDCs
            dSEntities.forEach((k, v)  -> v.bye(request));

            return buildGenericResponse(Messages.ResultCode.ERR_OK, request.getMessageId());
        } else {
            log.warn("bye() -- Dropped vdSM request. Unknown dSUID ({})", request.getVdsmSendPing().getDSUID());
            return null;
        }
    }

    public Messages.Message processRemove(Messages.Message request) {
        if(!request.hasVdsmSendRemove() || request.getVdsmSendRemove().hasDSUID()) {
            log.warn("remove() -- Incomplete request. No payload or dSUID");
            return null;
        }
        return null;
    }

    public void processGenericResponse(Messages.Message response) {
        if (!response.hasMessageId() || response.getMessageId() == RESERVED_REQUEST_ID) {
            log.error("genericResponse(): Invalid or unknown vdSM response (message id: '{}' / desc.: {})",
                    response.getMessageId(),
                    response.getGenericResponse().getDescription());

            return;
        }
        if (!response.hasGenericResponse() || response.getGenericResponse().hasCode()) {
            log.error("genericResponse(): Invalid vdSM response payload");
            return;
        }

        //find corresponding request and delegate its embedded callback method
        Request request = requestCache.get(response.hasMessageId());
        if(request != null) {
            request.callback().accept(response.getGenericResponse());
        }
    }

    /**
     * Send the given {@link vdcapi.Messages.Message } to connected vDSM
     *
     * @param message
     */
    public void send(Messages.Message message, Consumer<Messages.GenericResponse> cb) {
        if(isConnected() && message != null && message.isInitialized()) {
            Request request = Request.builder().id(message.getMessageId()).callback(cb).build();
            requestCache.put(request.id(), request);
            log.debug("[request-id: {}] send () msg: {}", request.id(), message);
            server.send(message);
        } else {
            log.error("send() -- Host not connected to vdSM or message not properly initialized --> Do nothing");
        }
    }

    public void sendError(Messages.Message message) {
        //TODO: ...
    }

//    public Messages.Message bye(Messages.Message request) {
//        if (request.hasVdsmSendBye()) {
//            log.warn("[vdSM <== bye()] {} says bye", getName());
//            return buildGenericResponse(Messages.ResultCode.ERR_OK, request.getMessageId());
//        } else {
//            log.warn("[vdSM ==> bye()] Incomplete vdSM request. No payload or dSUID");
//            return null;
//        }
//    }

    public void addVdc(@NotNull Vdc vDC) {
        Assert.notNull(vDC, "vDC must not 'null'");
        vDC.setHost(this);
        //put vDC as well as all of its managed dS devices into the overall dS entity map
        dSEntities.put(vDC.getDSUID(), vDC);
        vDC.getDevices().forEach(dSEntities::putIfAbsent);

        if(isConnected()) {
            //@todo: announce new vDC to connected vdSM
            vDC.announce();
            //announceVDC(vDC);
        }
    }

    public DsAddressable findEntity(DSUID dsUID) {
        return dSEntities.get(dsUID);
    }

    public int getMessageId() {
        if(!isConnected())
        {
            throw new IllegalStateException("vDC Host not connected to vdSM");
        }
        return session.getMessageId();
    }

    public boolean isConnected() {
        return server.isConnected() && session != null ? session.isConnected() : false ;
    }

    /**
     * start vDC Host
     */
    @PostConstruct
    public void start() throws InterruptedException, IOException {
        try {
            server.start(this);
            registerHost();
            waitUntilConnectionIsEstablished();

            //announce all already known vDCs
            log.info("start announcing vDC(s)");
            dSEntities.entrySet()
                    .stream()
                    .filter(entity -> entity instanceof Vdc)
                    .forEach(entity -> ((Vdc)entity).announce());
        } catch (InterruptedException | IOException e) {
            log.error("VdcHost failed to start, " + e.getMessage(), e);
            throw e;
        }
        log.info("VdcHost started");
    }

//    // Log start message
//    LOG(LOG_NOTICE,
//    "\n\n\n*** starting initialisation of vcd host '%s'\n*** dSUID (%s) = %s, MAC: %s, IP = %s\n",
//        publishedDescription().c_str(),
//    externalDsuid ? "external" : "MAC-derived",
//    shortDesc().c_str(),
//    macAddressToString(mac, ':').c_str(),
//    ipv4ToString(getIpV4Address()).c_str()
//  );
//    // start the API server if API is enabled
//  if (vdcApiServer) {
//        vdcApiServer->setConnectionStatusHandler(boost::bind(&VdcHost::vdcApiConnectionStatusHandler, this, _1, _2));
//        vdcApiServer->start();
//    }
//    // start initialisation of class containers
//    initializeNextVdc(aCompletedCB, aFactoryReset, vdcs.begin());

    /**
     *
     * @throws InterruptedException
     */
    private void waitUntilConnectionIsEstablished() throws InterruptedException {
        int waitingTime = 0;
        while(!isConnected()) {
            log.info("Waiting for vdSM sending 'hello()' for another 5 sec. ");
            TimeUnit.SECONDS.sleep(1);
            waitingTime=+1;
        }
        log.info("vdSM connection established after {} seconds", waitingTime);
    }

    /**
     * Register vDC Host as network service at mDNS so it can be discovered by a running vdSM in the same network.
     * Will be automatically called by Spring (Boot) framework on JVM after object initialization
     *
     * @throws IOException
     */
    private void registerHost() throws IOException {
        final String SERVICE_TYPE = "_ds-vdc._tcp.local."; // as defined by digitalSTROM Virtual-Device-Connector API
        try {
            log.debug("Initializing jmDNS (Avahi, Bonjour, Zeroconf, ...)");
            jmDNS = JmDNS.create(InetAddress.getLocalHost());

            final String serviceName = "digitalSTROM vDC host@" + jmDNS.getHostName().replace(".local.", "");
            ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, serviceName, server.getPort(), "dSUID=" + this.getDSUID());
            log.info("Registering vDC Server via jmDNS: {}", serviceInfo);

            jmDNS.registerService(serviceInfo);
        } catch (IOException exp) {
            log.error("Initializing JmDNS (Avahi, Bonjour, Zeroconf, ...) failed" + exp.getMessage(), exp);
            throw exp;
        }
        log.info("Vdc Host started [IP: {} (Mac: {}) -- Port:{}]", getIPAddress(), getMacAddress(), server.getPort());
    }

    /**
     * Unregister vDC Server from mDNS. Will be automatically called by Spring (Boot) framework on JVM shutdown
     */
    @PreDestroy
    private void unregisterHost() {
        if (jmDNS != null) {
            log.info("Unregister vDC Server via JmDBS");
            jmDNS.unregisterAllServices();
        }
        server.stop();
    }

    /**
     * Returns the MAC address of the underlying {@code ProtoBuf} host respective its bound network interface
     *
     * @return
     * @throws IOException
     */
    private String getMacAddress() throws IOException {
        if(jmDNS != null) {
            byte[] address = jmDNS.getInetAddress().getAddress();
            if (address == null)
                return null;
            String mac = "";
            for (int i = 0; i < address.length; i++) {
                mac += String.format("%x:", address[i]);
            }
            if (mac.length() > 0 && !jmDNS.getInetAddress().isLoopbackAddress()) {
                return mac.toLowerCase().substring(0, mac.length() - 1);
            }
        }
        return null;
    }

    /**
     * Returns the IP address string in textual presentation
     *
     * @return the raw IP address in a string format.
     */
    private String getIPAddress() throws IOException {
        if(jmDNS != null) {
            return jmDNS.getInetAddress().getHostAddress();
        }
        return "";
    }
}