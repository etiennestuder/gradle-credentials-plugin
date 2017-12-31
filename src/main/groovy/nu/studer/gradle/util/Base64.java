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
        return org.apache.commons.codec.binary.Base64.encodeBase64String(bytes);
//        return StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(bytes, false));
    }

    /**
     * Decodes the given Base64 string to bytes.
     *
     * @param string the Base64 string to decode
     * @return the resulting bytes
     */
    public static byte[] decodeBase64(String string) {
        return org.apache.commons.codec.binary.Base64.decodeBase64(string);
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

}
