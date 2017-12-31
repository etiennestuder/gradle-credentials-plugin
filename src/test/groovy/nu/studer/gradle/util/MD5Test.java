package nu.studer.gradle.util;

import org.junit.Test;

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

}
