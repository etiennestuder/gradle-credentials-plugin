package nu.studer.gradle.util;

import javax.xml.bind.DatatypeConverter;

/**
 * Utilities related to Base64 encoding.
 */
public final class Base64 {

    private Base64() {
    }

    /**
     * Encodes the given bytes to a Base64 string.
     *
     * @param bytes the bytes to encode
     * @return the resulting Base64 string
     */
    public static String encodeBase64(byte[] bytes) {
        return DatatypeConverter.printBase64Binary(bytes);
    }

    /**
     * Decodes the given Base64 string to bytes.
     *
     * @param string the Base64 string to decode
     * @return the resulting bytes
     */
    public static byte[] decodeBase64(String string) {
        return DatatypeConverter.parseBase64Binary(string);
    }

}
