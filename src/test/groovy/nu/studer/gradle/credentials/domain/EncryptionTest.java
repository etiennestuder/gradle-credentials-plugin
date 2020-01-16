package nu.studer.gradle.credentials.domain;

import nu.studer.gradle.credentials.CredentialsPlugin;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class EncryptionTest {

    @Test
    public void encryptDecryptAcrossDifferentEncryptionInstances() {
        char[] passphrase = "My very secret pass phrase".toCharArray();
        String textToEncrypt = "Some text that needs to be encrypted.";

        Crypto.Encryption encryption = Crypto.createEncryption(passphrase);
        String encrypted = encryption.encrypt(textToEncrypt);
        System.out.printf("0Encrypted: %s\n", encrypted);
        Crypto.Encryption encryption2 = Crypto.createEncryption(passphrase);
        String encrypted2 = encryption.encrypt(textToEncrypt);
        System.out.printf("1Encrypted: %s\n", encrypted2);
        String decrypted = encryption2.decrypt(encrypted);
        System.out.printf("Decrypted: %s\n", decrypted);

        assertEquals(textToEncrypt, decrypted);
    }
    @Test
    public void encryptionResultDifferentWithSameKey() {
        char[] passphrase = "My very secret pass phrase".toCharArray();
        String textToEncrypt = "Some text that needs to be encrypted.";

        Crypto.Encryption encryption = Crypto.createEncryption(passphrase);
        String encrypted = encryption.encrypt(textToEncrypt);
        String encrypted2 = encryption.encrypt(textToEncrypt);
        System.out.printf("0Encrypted: %s\n", encrypted);
        System.out.printf("1Encrypted: %s\n", encrypted2);

        assertNotEquals(encrypted, encrypted2);
    }

    @Test
    public void credentialsKeyHashForProperty() {
        char[] passphrase = "My very secret pass phrase".toCharArray();
        String key = "someKey";

        Crypto.Encryption encryption = Crypto.createEncryption(passphrase);
        String hashedKey = encryption.hash(key);

        assertNotEquals(key, hashedKey);

    }

    @Test
    public void credentialsKeyHashForPropertyRepeatable() {
        char[] passphrase = CredentialsPlugin.DEFAULT_PASSPHRASE.toCharArray();
        String key = "someKey";

        Crypto.Encryption encryption = Crypto.createEncryption(passphrase);
        String hashedKey = encryption.hash(key);
        Crypto.Encryption encryption0 = Crypto.createEncryption(passphrase);
        String hashedKey0 = encryption0.hash(key);
        System.out.println(hashedKey);

        assertEquals(hashedKey0, hashedKey);

    }

    @Test
    public void credentialsBackwardsCompatibilityWithv2_1() {
        char[] passphrase = CredentialsPlugin.DEFAULT_PASSPHRASE.toCharArray();
        String value = "someValue";
        String v2_1EncryptedValue = "bJhu1+pR6F6mNbBzAEWKRA\\=\\=";

        Crypto.Encryption encryption = Crypto.createEncryption(passphrase);
        String decryptedValue = encryption.decrypt(v2_1EncryptedValue);

        assertEquals(decryptedValue, value);

    }


    @Test
    public void credentialsKeyHashForPropertyDifferentForDifferentPassphrases() {
        char[] passphrase = "My very secret pass phrase".toCharArray();
        char[] passphrase0 = "My other very secret pass phrase".toCharArray();
        String key = "someKey";

        Crypto.Encryption encryption = Crypto.createEncryption(passphrase);
        String hashedKey = encryption.hash(key);
        Crypto.Encryption encryption0 = Crypto.createEncryption(passphrase0);
        String hashedKey0 = encryption0.hash(key);

        assertNotEquals(hashedKey0, hashedKey);

    }
}
