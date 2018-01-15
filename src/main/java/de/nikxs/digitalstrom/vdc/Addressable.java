package de.nikxs.digitalstrom.vdc;

import de.nikxs.digitalstrom.vdc.util.DSUID;
import de.nikxs.digitalstrom.vdc.util.DsUtil;
import org.slf4j.Logger;
import vdcapi.Messages;

import static org.slf4j.LoggerFactory.getLogger;


public interface Addressable {

    //byte[] png16x16 = new byte[0];

    DSUID getDSUID();

    String getName();

    /**
     * Default implementation of pong() method
     *
     * @param request
     * @return pong() response
     */
    default Messages.Message pong(Messages.Message request) {
        if (request.hasVdsmSendPing()) {
            LogHolder.LOGGER.info("pong() -- [default implementation] '{}' ({}) says pong", getName(), getDSUID());
            return Messages.Message.newBuilder()
                    .setType(Messages.Type.VDC_SEND_PONG)
                    .setVdcSendPong(request.getVdcSendPong().toBuilder().setDSUID(getDSUID().toString()).build())
                    .build();
        } else {
            LogHolder.LOGGER.warn("pong() -- [default implementation] Incomplete vdSM ping() request.");
            return null;
        }
    }

    /**
     * Default implementation of bye() method
     *
     * @param request
     * @return pong() response
     */
    default Messages.Message bye(Messages.Message request) {
        if (request.hasVdsmSendBye()) {
            LogHolder.LOGGER.info("bye() -- [default implementation] '{}' ({}) > do nothing...", getName(), getDSUID());
            return DsUtil.buildGenericResponse(Messages.ResultCode.ERR_OK, request.getMessageId());
        } else {
            LogHolder.LOGGER.warn("bye() -- [default implementation] Incomplete vdSM bye() request.");
            return null;
        }
    }

    //void vanish(Addressable device);
}

final class LogHolder { // not public
    static final Logger LOGGER;

    static {
        LOGGER = getLogger(Addressable.class);
    }
}
