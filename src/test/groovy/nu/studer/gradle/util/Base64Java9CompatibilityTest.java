package nu.studer.gradle.util;

import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public final class Base64Java9CompatibilityTest {

    @Test
    public void librariesAreCompatibleInEncoding() {
        byte[] bytes = "some text".getBytes(StandardCharsets.UTF_8);
        assertEquals(Base64.encodeBase64(bytes), DatatypeConverter.printBase64Binary(bytes));
    }

    @Test
    public void librariesAreCompatibleInDecoding() {
        byte[] bytes = "some text".getBytes(StandardCharsets.UTF_8);
        String encoded = DatatypeConverter.printBase64Binary(bytes);
        assertArrayEquals(Base64.decodeBase64(encoded), DatatypeConverter.parseBase64Binary(encoded));
    }

    @Test
    public void librariesAreCompatibleInPrinting() {
        byte[] bytes = "some text".getBytes(StandardCharsets.UTF_8);
        assertEquals(Base64.printHexBinary(bytes), DatatypeConverter.printHexBinary(bytes));
    }

}
