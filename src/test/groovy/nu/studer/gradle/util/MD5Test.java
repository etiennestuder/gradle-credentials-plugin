package nu.studer.gradle.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MD5Test {

    @Test
    public void generateMD5Hash() {
        String input = "some string";

        String result = MD5.generateMD5Hash(input);

        for (char c : result.toCharArray()) {
            boolean isHex = '0' <= c && c <= '9' || 'A' <= c && c <= 'F';
            assertTrue("Character must be HEX: " + c, isHex);
        }
    }

    @Test
    public void convertByteArrayToHexString() {
        byte[] bytes = new byte[]{-128, -1, 0, 9, 10, 11, 17, 127};
        String hex = Base64.printHexBinary(bytes);
        assertEquals("80FF00090A0B117F", hex);
    }

}
