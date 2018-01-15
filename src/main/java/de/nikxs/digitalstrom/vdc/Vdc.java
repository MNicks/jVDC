package de.nikxs.digitalstrom.vdc;

import de.nikxs.digitalstrom.vdc.util.DSUID;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import vdcapi.Messages;
import vdcapi.Vdcapi;

import java.util.HashMap;

@Slf4j
public abstract class Vdc extends Entity {

    @Getter
    @Setter
    private int zoneID;

    @Getter
    @Setter
    private String capabilities;


    @Getter
    @Setter
    private boolean connected;

    /**
     * List of all dS devices managed by this vDC
     */
    @Getter private HashMap<DSUID, Addressable> devices = new HashMap<>();

    /**
     * Reference to ProtocolBuffer server implementation used as communication layer to/from vdSM
     */
    @Getter @Setter private VdcHost host;

    public Vdc(String model) {
        this(DSUID.generateV4(), model);
    }

    public Vdc(DSUID dSUID, String model) {
        super(Type.VDC, dSUID, model);
    }

    /**
     * Announce this vDC to the connected vdSM
     */
    public void announce() {
        Messages.Message.Builder message =
                Messages.Message.newBuilder()
                        .setType(Messages.Type.VDC_SEND_ANNOUNCE_VDC)
                        .setMessageId(host.getMessageId());

        Vdcapi.vdc_SendAnnounceVdc announceVDC =
                message.getVdcSendAnnounceVdc().toBuilder().setDSUID(getDSUID().toString()).build();
        message.setVdcSendAnnounceVdc(announceVDC);

        log.info("announce() -- vDC '{}' ({})", getModel(), this.getDSUID());
        host.send(message.build(), this::announceCB );
    }

    public abstract void announceCB(Messages.GenericResponse response);

    /**
     * informs the connected vdSM that the given {@link Addressable} has vanished from this vDC
     * @param device
     */
    public void vanish(Addressable device) {
        Messages.Message.Builder message = Messages.Message.newBuilder();
        message.setType(Messages.Type.VDC_SEND_VANISH);

        Vdcapi.vdc_SendVanish vanishVdSD =
                message.getVdcSendVanish().toBuilder().setDSUID(device.getDSUID().toString()).build();
        message.setVdcSendVanish(vanishVdSD);
        //TODO ... -> host.send(message.build());
    }


//    public rememberSomething() {
//        byte[] bArray = {
//                0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00, 0x00, 0x00, 0x0d,
//                0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x10,
//                0x08, 0x06, 0x00, 0x00, 0x00, 0x1f, 0xf3, 0xff, 0x61, 0x00, 0x00, 0x00,
//                0x06, 0x62, 0x4b, 0x47, 0x44, 0x00, 0xff, 0x00, 0xff, 0x00, 0xff, 0xa0,
//                0xbd, 0xa7, 0x93, 0x00, 0x00, 0x00, 0x09, 0x70, 0x48, 0x59, 0x73, 0x00,
//                0x00, 0x0b, 0x13, 0x00, 0x00, 0x0b, 0x13, 0x01, 0x00, 0x9a, 0x9c, 0x18,
//                0x00, 0x00, 0x00, 0x07, 0x74, 0x49, 0x4d, 0x45, 0x07, 0xde, 0x08, 0x15,
//                0x09, 0x19, 0x31, 0x76, 0x58, 0xa9, 0x3e, 0x00, 0x00, 0x01, 0x2a, 0x49,
//                0x44, 0x41, 0x54, 0x38, 0xcb, 0xcd, 0x93, 0xbd, 0x4a, 0x03, 0x51, 0x14,
//                0x84, 0xbf, 0xd9, 0xc4, 0x22, 0x92, 0xc2, 0x58, 0x59, 0x28, 0x42, 0x1e,
//                0x40, 0xac, 0xdc, 0x55, 0x10, 0x5b, 0x6d, 0x74, 0xd7, 0x37, 0xf0, 0x49,
//                0xc4, 0x87, 0xb0, 0xb2, 0xb1, 0xb1, 0x75, 0x63, 0xa1, 0xe0, 0x13, 0x64,
//                0xbb, 0x14, 0x82, 0xe2, 0x4f, 0xa7, 0x45, 0x0a, 0x45, 0x11, 0xd4, 0x80,
//                0x66, 0xc7, 0x46, 0x12, 0xa3, 0xe4, 0xa7, 0xcc, 0x54, 0xe7, 0x70, 0xcf,
//                0x1d, 0xe6, 0xcc, 0x9d, 0x0b, 0xe3, 0x83, 0x24, 0x1a, 0xdc, 0xf7, 0x41,
//                0xd0, 0xa9, 0x6c, 0xf5, 0x9c, 0xfc, 0xed, 0xfb, 0xa0, 0x3b, 0x94, 0x84,
//                0x53, 0xa0, 0x3d, 0xa0, 0x0a, 0x3e, 0xe7, 0xbd, 0xb4, 0xcf, 0x64, 0xcb,
//                0xa4, 0xd9, 0x08, 0x04, 0x71, 0xb4, 0x0e, 0x3e, 0x45, 0x04, 0x20, 0x03,
//                0xc2, 0x7e, 0x40, 0x5a, 0x04, 0x9e, 0x06, 0x91, 0x14, 0x48, 0xa2, 0x12,
//                0xe2, 0x12, 0x94, 0x03, 0x01, 0x52, 0x8e, 0x09, 0x90, 0xca, 0xc0, 0x1c,
//                0x69, 0x76, 0x3c, 0x48, 0x41, 0x11, 0xda, 0xc2, 0x85, 0x4f, 0xa0, 0x89,
//                0x94, 0x80, 0xae, 0x91, 0x63, 0xec, 0x43, 0xa4, 0x0f, 0xe2, 0x68, 0x05,
//                0xfc, 0x48, 0x4d, 0x37, 0x90, 0xfd, 0x36, 0x79, 0x03, 0x78, 0x0d, 0xb0,
//                0x26, 0x10, 0x0d, 0x6a, 0xd9, 0x3c, 0x76, 0x83, 0xb4, 0xfe, 0x46, 0x9a,
//                0x1d, 0x61, 0x57, 0x81, 0x2f, 0xa0, 0x8c, 0x74, 0x45, 0xcc, 0x6a, 0xe7,
//                0x72, 0x1c, 0xed, 0x80, 0xcf, 0x80, 0x5c, 0x6c, 0x85, 0x45, 0x0a, 0x2f,
//                0x6d, 0x5c, 0xe9, 0x35, 0x2c, 0x59, 0x86, 0xb4, 0x0e, 0x71, 0xb8, 0x89,
//                0x74, 0x02, 0x06, 0xeb, 0x16, 0x5c, 0x42, 0x9a, 0xc5, 0x00, 0x5e, 0x1b,
//                0xfe, 0x54, 0xdb, 0xd1, 0x34, 0xe6, 0x0e, 0xa8, 0x60, 0x77, 0xbd, 0x17,
//                0x17, 0x28, 0x5f, 0x0a, 0x86, 0x12, 0xe4, 0x3c, 0x13, 0x68, 0x06, 0x7c,
//                0x80, 0xd4, 0x04, 0xee, 0x91, 0x77, 0x49, 0xb3, 0x05, 0x1c, 0xb4, 0x46,
//                0x4d, 0xe9, 0x7f, 0xa5, 0x71, 0xa8, 0xde, 0x24, 0x0e, 0x86, 0x3b, 0xf1,
//                0x8e, 0xc3, 0x9f, 0x2d, 0xe4, 0xf1, 0xf8, 0x83, 0xdf, 0x9f, 0x72, 0x65,
//                0x87, 0xbf, 0x51, 0x22, 0xbd, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4e,
//                0x44, 0xae, 0x42, 0x60, 0x82 };
//        BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
//
//        Path path = Paths.get("path/to/file");
//        byte[] data = Files.readAllBytes(path);
//        IOUtils.toByteArray(InputStream input);
//    }
}
