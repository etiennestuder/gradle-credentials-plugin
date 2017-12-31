package nu.studer.gradle.util;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Base64Test {

    @Test
    public void encodeDecode() throws UnsupportedEncodingException {
        byte[] textToEncode = "Some text that needs to be encoded.".getBytes("UTF-8");

        String encoded = Base64.encodeBase64(textToEncode);
        byte[] decoded = Base64.decodeBase64(encoded);

        assertArrayEquals(textToEncode, decoded);
    }

    @Test
    public void printHexBinary() {
        byte[] bytes = new byte[]{-128, -1, 0, 9, 10, 11, 17, 127};
        String hex = Base64.printHexBinary(bytes);
        assertEquals("80FF00090A0B117F", hex);
    }

}
