package nu.studer.gradle.credentials.domain;

/**
 * Encrypts/decrypts credentials through password-based encryption.
 *
 * @see Crypto.Encryption
 */
public final class CredentialsEncryptor {

    private final Crypto.Encryption encryption;

    private CredentialsEncryptor(Crypto.Encryption encryption) {
        this.encryption = encryption;
    }

    /**
     * Encrypts the given string.
     *
     * @param string the string to encrypt
     * @return the encrypted string
     */
    public String encrypt(String string) {
        return string != null ? encryption.encrypt(string) : null;
    }

    /**
     * Creates a new instance that will use the given passphrase for all encryption/decryption activities.
     *
     * @param passphrase the passphrase to encrypt/decrypt the credentials with
     * @return the new instance
     */
    public static CredentialsEncryptor withPassphrase(char[] passphrase) {
        Crypto.Encryption encryption = Crypto.createEncryption(passphrase);
        return new CredentialsEncryptor(encryption);
    }

    /**
     * Decrypts the given string.
     *
     * @param string the string to decrypt
     * @return the decrypted string
     */
    public String decrypt(String string) {
        return string != null ? encryption.decrypt(string) : null;
    }

    /**
     * Hashes the input key based on the passphrase given when this instance was created.
     *
     * @param key the gradle credentials properties key to hash
     * @return a hash of the input key and the passphrase used to construct this instance
     */
    public String hash(String key) {
        return key != null ? encryption.hash(key) : null;
    }

}
