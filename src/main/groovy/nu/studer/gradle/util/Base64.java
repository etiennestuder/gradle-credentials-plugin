package nu.studer.gradle.util;

import static org.apache.commons.codec.binary.Hex.encodeHex;

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
        return base64Instance().encodeToString(bytes);
    }

    /**
     * Decodes the given Base64 string to bytes.
     *
     * @param string the Base64 string to decode
     * @return the resulting bytes
     */
    public static byte[] decodeBase64(String string) {
        return base64Instance().decode(string);
    }

    /**
     * Print the given bytes as a HEX string.
     *
     * @param bytes the bytes to covert to a HEX string
     * @return the resulting string
     */
    public static String printHexBinary(byte[] bytes) {
        return new String(encodeHex(bytes, false));
    }

    private static org.apache.commons.codec.binary.Base64 base64Instance() {
        return new org.apache.commons.codec.binary.Base64(0, new byte[]{'\r', '\n'}, false);
    }

}
