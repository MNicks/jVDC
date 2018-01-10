package de.nikxs.digitalstrom.vdc.util;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;

//@RunWith(SpringJUnit4ClassRunner.class)
public class DSUIDTest {

//    @Test
//    public void version() {
//        DSUID randomdSUID = DSUID.generateV4();
//        Assert.assertEquals("Expected DSUID/UUID version to be '4'", 4, randomdSUID.version());
//
//        DSUID namespacedSUID = DSUID.generateV3(DSUID.NamespaceUUID.ENOCEAN, "something");
//        Assert.assertEquals("Expected DSUID/UUID version to be '3'", 3, namespacedSUID.version());
//
//        DSUID timebaseddSUID = DSUID.generateV1();
//        Assert.assertEquals("Expected DSUID/UUID version to be '1'", 1, timebaseddSUID.version());
//        //byte[] result = UuidAdapter.getBytesFromUUID(uuid);
//    }

    @Test
    public void should_Get_16_Bytes_From_a_UUID() {
        DSUID dsUID1 = DSUID.generateV4();

        byte[] dsuidBytes = dsUID1.toString().getBytes(Charset.forName("UTF-8"));

        Assert.assertEquals("Expected result to be a byte array w/17 elements.", 17, dsuidBytes.length/2);
    }

    @Test
    public void should_Reconstruct_Same_DSUID_from_DSUID_String() {
        DSUID dsUID1 = DSUID.generateV4();
        String strDsuid1 = dsUID1.toString();

        DSUID dsUID2 = DSUID.fromDSUID(strDsuid1);
        String strDsuid2 = dsUID2.toString();

        Assert.assertEquals(strDsuid1,strDsuid2);
        Assert.assertEquals(dsUID1, dsUID2);
    }
//
//    @Test
//    public void compareTo() {
//    }
//
//    @Test
//    public void fromDSUID() {
//    }
}