package de.nikxs.digitalstrom.vdc;

import de.nikxs.digitalstrom.vdc.util.DSUID;
import de.nikxs.digitalstrom.vdc.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;

public abstract class Entity implements Addressable {

    /**
     * Allowed entity types
     */
    @ToString
    public enum Type {
        /**
         * virtual digtalSTROM device. A vdSD represents a dS-Device system
         */
        VDSD("vdSD"),
        /**
         * A virtual device connector (vDC) is primarily a logical entity within the dS system and has its own dSUID.
         * A vDC connects to a Native IP Device or to a IP Gateway with Native Gateway Connected Devices.
         */
        VDC("vDC"),
        /**
         * Network device offering a server socket for vdsm to connect to. One vDC host can host multiple
         * logical vDCs, if the host supports multiple device classes.he vDC host
         */
        VDC_HOST("vDChost"),
        /**
         * virtual digitalSTROM Meter (vdSM). A vdSM can connect to one or several vDC hosts to connect one or
         * several logical vDCs to the dS system.
         */
        VDSM("vdSM");

        @Getter
        private final String type;

        /**
         * @param type
         */
        Type(final String type) {
            this.type = type;
        }

    }

    /**
     * the dSUID of the entity
     */
    @Getter
    private DSUID dSUID;

    /**
     * the type of the entity
     */
    @Getter
    private Type type;

    /**
     * Human-readable model string of the entity. Should be descriptive enough to allow a human to associate
     * it with a kind of hardware or software. Is mapped to “hardwareInfo” in vdsm and upstream
     */
    @Getter
    private String model;

    /**
     * Human-readable model version string of the device, if available
     */
    @Getter
    private String modelVersion;

    /**
     * digitalSTROM system unique ID for the functional model of the entity.
     *
     * - modelUID must be equal between all functionally identical entities (especially, devices) dS system.
     * - If different connected hardware devices provide EXACTLY the same dS functionality, these devices MAY have
     *   the same modelUID but will have different hardwareModelGuid.
     * - Vice versa, for example two identical hardware input devices will have the same hardwareModelGuid,
     *   but different modelUID if one input is mapped as a button, and the other as a binaryInput.
     */
    @Getter
    private String modelUID;

    /**
     * string describing the model’s version as seen by the end user (usually the firmware version of the vdc host)
     */
    @Getter
    @Optional
    private String modelVersion2;

    /**
     * Human-readable string describing the hardware device’s version represented by this entity, if available
     */
    @Getter
    @Optional
    private String hardwareVersion;

    /**
     * hardware’s native globally unique identifier (GUID), if any, in URN-like format: formatname:actualID
     *
     * The following formats are in use:
     * - gs1:(01)ggggg(21)sssss = GS.1 formatted GTIN plus serial number
     * - macaddress:MMMMM = MAC Address
     * - enoceanaddress:XXXXXXXX = 8 hex digits EnOcean device address
     * - uuid:UUUUUUU = UUID
     */
    @Getter
    @Optional
    private String hardwareGuid;

    /**
     * hardware model’s native globally unique identification, if any, in URNlike format: formatname:actualID
     *
     * The following formats are in use:
     * - gs1:(01)ggggg = GS.1 formatted GTIN
     * - enoceaneep:oofftt = 6 hex digits EnOcean Equipment Profile (EEP) number
     */
    @Getter
    @Optional
    private String hardwareModelGuid;

    /**
     * Human-readable string of the device manufacturer or vendor
     */
    @Getter
    @Optional
    private String vendorName;

    /**
     * globally unique identification of the vendor, in URN-like format:
     *
     * The following formats are in use:
     * - enoceanvendor:vvv[:name] = 3 hex digits EnOcean vendor ID, optionally followed by a colon and the
     *   clear text vendor name if known
     * - vendorname:name = clear text name of the vendor
     * - gs1:(412)lllll = GS1 formatted Global Location Number of the vendor
     */
    @Getter
    @Optional
    private String vendorGuid;

    /**
     * Globaly unique identifier (GUID) of the product the hardware is embedded in,
     * if any - see hardwareGuid for format variants
     */
    @Getter
    @Optional
    private String oemGuid;

    /**
     * full URL how to reach the web configuration of this device (if any)
     */
    @Getter
    @Optional
    private String configURL;

    /**
     * 16x16 pixel png image to represent this device in the digitalSTROM configurator UI
     */
    @Getter
    @Optional
    private byte[] deviceIcon16;

    /**
     * filename-safe name for the icon (a-z, 0-9, _, -, no spaces or funny characters!).
     * This allows for more efficient caching in Web UIs - many devices might have the same icon,
     * so web UIs don’t need to load the actual data (deviceIcon16) for every device again, as long as
     * devices show the same deviceIconName.
     */
    @Getter
    @Optional
    private String deviceIconName;

    /**
     * user-specified name of the entity. Is also stored upstreams in the vdSM and further up, but is useful for
     * the vDC to know for configuration and debugging.
     * The vDC usually generates descriptive default names for newly connected devices. When the vdSM registers a
     * device, it should read this property and propagate the name towards the dSS. When the user changes the name
     * via the dSS configurator, this property should be updated with the new name.
     */
    @Getter
    @Setter
    @Value("${vdc.host.name:'default-vDC-Host'}")
    private String name;

    /**
     * digitalSTROM defined unique name of a device class profile
     */
    @Getter
    @Optional
    private String deviceClass;

    /**
     * revision number of the device class profile
     */
    @Getter
    @Optional
    private String deviceClassVersion;

    public Entity(Type type, DSUID dsuid, String model) {
        this.type = type;
        this.dSUID = dsuid;
        this.model = model;
    }
}
