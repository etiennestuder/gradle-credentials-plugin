package nu.studer.gradle.util;

import java.security.MessageDigest;

/**
 * Utilities related to MD5 hashing.
 */
public final class MD5 {

    private MD5() {
    }

    /**
     * Calculates the MD5 hash for the given string and returns it in HEX format.
     *
     * @param string the string to hash
     * @return the resulting MD5 hash as a string in HEX format
     */
    public static String generateMD5Hash(String string) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hashedBytes = digest.digest(string.getBytes("UTF-8"));
            return Base64.printHexBinary(hashedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate MD5 hash for string '" + string + "': " + e.getMessage(), e);
        }
    }

}
