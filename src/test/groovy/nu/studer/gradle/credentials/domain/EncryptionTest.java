package nu.studer.gradle.credentials.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EncryptionTest {

    @Test
    public void encryptDecryptAcrossDifferentEncryptionInstances() {
        char[] passphrase = "My very secret pass phrase".toCharArray();
        String textToEncrypt = "Some text that needs to be encrypted.";

        Encryption encryption = Encryption.createEncryption(passphrase);
        String encrypted = encryption.encrypt(textToEncrypt);

        Encryption encryption2 = Encryption.createEncryption(passphrase);
        String decrypted = encryption2.decrypt(encrypted);

        assertEquals(textToEncrypt, decrypted);
    }

}
