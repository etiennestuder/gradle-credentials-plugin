package nu.studer.gradle.credentials.domain;

import nu.studer.gradle.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

/**
 * Encryption/decryption of text using ciphers.
 * <p>
 * Note: The author of this class is by far not a security expert. The chosen implementation has been primarily gathered from examples in the javax.crypto Javadoc and from
 * discussions on StackOverflow.
 * <p>
 * See also <a href="http://stackoverflow.com/questions/992019/java-256-bit-aes-password-based-encryption">here</a> for a more detailed explanation on the selected security
 * algorithms.
 */
public final class Encryption {

    private static final String UTF_8_CHARSET = "UTF8";

    private final Cipher ecipher;
    private final Cipher dcipher;

    public Encryption(Cipher ecipher, Cipher dcipher) throws GeneralSecurityException {
        this.ecipher = ecipher;
        this.dcipher = dcipher;
    }

    /**
     * Encrypts the given text.
     *
     * @param string the text to encrypt
     * @return the encrypted text
     */
    public String encrypt(String string) {
        try {
            byte[] utf8 = string.getBytes(UTF_8_CHARSET);
            byte[] enc = ecipher.doFinal(utf8);
            return Base64.encodeBase64(enc);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts the given text.
     *
     * @param string the text to decrypt
     * @return the decrypted text
     */
    public String decrypt(String string) {
        try {
            byte[] dec = Base64.decodeBase64(string);
            byte[] utf8 = dcipher.doFinal(dec);
            return new String(utf8, UTF_8_CHARSET);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new Encryption instance that uses password-based encryption (PBE). The algorithm used to create the secret key is <i>PBEWithMD5AndDES</i>.
     *
     * @param passphrase the passphrase to apply when creating the secret key
     * @return the new Encryption instance
     * @throws RuntimeException with wrapped GeneralSecurityException in case of crypto-related exceptions
     */
    public static Encryption createEncryption(char[] passphrase) {
        try {
            return createEncryptionThrowingException(passphrase);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Cannot create Encryption instance: " + e.getMessage(), e);
        }
    }

    private static Encryption createEncryptionThrowingException(char[] passphrase) throws GeneralSecurityException {
        // define a salt to prevent dictionary attacks (ideally, the salt would be
        // regenerated each time and stored alongside the encrypted text)
        byte[] salt = {
                (byte) 0x1F, (byte) 0x13, (byte) 0xE5, (byte) 0xB2,
                (byte) 0x49, (byte) 0x2C, (byte) 0xC3, (byte) 0x3C
        };

        // use a high iteration count to slow down the decryption speed
        int iterationCount = 65536;

        // use the maximum key length that does not require to install the JRE Security Extension
        int keyLength = 128;

        // provide password, salt, iteration count, and key length for generating the PBEKey
        KeySpec pbeKeySpec = new PBEKeySpec(passphrase, salt, iterationCount, keyLength);

        // create a secret (symmetric) key using PBE with SHA1 and AES
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        SecretKey tmpKey = keyFac.generateSecret(pbeKeySpec);
        SecretKey pbeKey = new SecretKeySpec(tmpKey.getEncoded(), "AES");

        // create a fixed iv spec that can be used both for encryption and for later decryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        int blockSize = cipher.getBlockSize();
        byte[] iv = new byte[blockSize];
        for (int i = 0; i < iv.length; i++) {
            iv[i] = (byte) i;
        }
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);

        // initialize the encryption cipher
        Cipher pbeEcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeEcipher.init(Cipher.ENCRYPT_MODE, pbeKey, ivSpec);

        // initialize the decryption cipher
        Cipher pbeDcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeDcipher.init(Cipher.DECRYPT_MODE, pbeKey, ivSpec);

        return new Encryption(pbeEcipher, pbeDcipher);
    }

}
