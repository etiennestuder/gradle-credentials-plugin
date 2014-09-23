package nu.studer.gradle.credentials;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EncryptionTest {

    @Test
    public void encryptDecrypt() throws Exception {
        char[] passPhrase = "My very secret pass phrase".toCharArray();
        String textToEncrypt = "Some text that needs to be encrypted.";

        Encryption encryption = Encryption.createEncryption(passPhrase);
        String encrypted = encryption.encrypt(textToEncrypt);
        String decrypted = encryption.decrypt(encrypted);

        assertEquals(textToEncrypt, decrypted);
    }

}
