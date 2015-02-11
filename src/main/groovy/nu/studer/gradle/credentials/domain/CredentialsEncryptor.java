package nu.studer.gradle.credentials.domain;

/**
 * Encrypts/decrypts credentials through password-based encryption.
 *
 * @see Encryption
 */
public final class CredentialsEncryptor implements Encryptor {

    private final Encryption encryption;

    private CredentialsEncryptor(Encryption encryption) {
        this.encryption = encryption;
    }

    /**
     * Encrypts the given string.
     *
     * @param string the string to encrypt
     * @return the encrypted string
     */
    public String encrypt(String string) {
        return encryption != null && string != null ? encryption.encrypt(string) : null;
    }

    /**
     * Decrypts the given string.
     *
     * @param string the string to decrypt
     * @return the decrypted string
     */
    public String decrypt(String string) {
        return encryption != null && string != null ? encryption.decrypt(string) : null;
    }

    /**
     * Creates a new instance that will use the given passphrase for all encryption/decryption activities.
     *
     * @param passphrase the passphrase to encrypt/decrypt the credentials with
     * @return the new instance
     */
    public static CredentialsEncryptor withPassphrase(char[] passphrase) {
        Encryption encryption = Encryption.createEncryption(passphrase);
        return new CredentialsEncryptor(encryption);
    }

}
