package nu.studer.gradle.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EncryptionTest {

    @Test
    public void encryptDecrypt() {
        char[] passPhrase = "My very secret pass phrase".toCharArray();
        String textToEncrypt = "Some text that needs to be encrypted.";

        Encryption encryption = Encryption.createEncryption(passPhrase);
        String encrypted = encryption.encrypt(textToEncrypt);
        String decrypted = encryption.decrypt(encrypted);

        assertEquals(textToEncrypt, decrypted);
    }

}
