package de.nikxs.digitalstrom;

import de.nikxs.digitalstrom.vdc.Vdc;
import de.nikxs.digitalstrom.vdc.util.DSUID;
import vdcapi.Messages;

public class MyTestVdc extends Vdc {

    public MyTestVdc(String name) {
        super(name);
    }

    public MyTestVdc(String name, DSUID dsuid) {
        super(name, dsuid);
    }

    @Override
    public void announceCB(Messages.GenericResponse response) {
        if(response.getCode() == Messages.ResultCode.ERR_OK) {
            setConnected(true);
        }
    }

}
