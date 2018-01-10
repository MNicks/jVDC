package de.nikxs.digitalstrom.vdc.server;

import de.nikxs.digitalstrom.vdc.util.DSUID;
import lombok.Getter;

public class Session {

    /**
     * Indicator whether vDC host is connected to a vdSM
     */
    @Getter private boolean connected;

    /**
     * vdSM (dSUID) this connection belongs to
     */
    @Getter private DSUID vdSMdSUID;

    private int messageCounter = 0; //?!?!


    /**
     * Constructs a virtual session to a vdSM with the given DSUID
     * @param vdSMdSUID
     */
    public Session(DSUID vdSMdSUID) {
        this.vdSMdSUID = vdSMdSUID;
        this.connected = true;
    }

    /**
     * Invalidates this sessi
     */
    public void invalidate() {
        connected = false;
        vdSMdSUID =  null;
    }

    public int getMessageId() {
        return ++messageCounter;
    }

    public boolean isVdsm(DSUID dSUID) {
        return vdSMdSUID.equals(dSUID);
    }
}
