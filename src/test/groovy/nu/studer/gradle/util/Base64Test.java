package nu.studer.gradle.util;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertArrayEquals;

public class Base64Test {

    @Test
    public void encodeDecode() throws UnsupportedEncodingException {
        byte[] textToEncode = "Some text that needs to be encoded.".getBytes("UTF-8");

        String encoded = Base64.encodeBase64(textToEncode);
        byte[] decoded = Base64.decodeBase64(encoded);

        assertArrayEquals(textToEncode, decoded);
    }

}
