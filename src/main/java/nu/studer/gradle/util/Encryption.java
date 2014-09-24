package nu.studer.gradle.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.GeneralSecurityException;

/**
 * Encryption/decryption of text using ciphers.
 * <p/>
 * Note: The author of this class is by far not a security expert. The chosen implementation has been primarily gathered from examples in the javax.crypto Javadoc.
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
     * @param passPhrase the password to apply when creating the secret key
     * @return the new Encryption instance
     * @throws RuntimeException with wrapped GeneralSecurityException in case of crypto-related exceptions
     */
    public static Encryption createEncryption(char[] passPhrase) {
        try {
            return createEncryptionThrowingException(passPhrase);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Cannot create Encryption instance: " + e.getMessage(), e);
        }
    }

    private static Encryption createEncryptionThrowingException(char[] passPhrase) throws GeneralSecurityException {
        // define salt to prevent dictionary attacks (ideally, the salt would be regenerated each time and stored alongside the encrypted text)
        byte[] salt = {
                (byte) 0x1F, (byte) 0x13, (byte) 0xE5, (byte) 0xB2,
                (byte) 0x49, (byte) 0x2C, (byte) 0xC3, (byte) 0x3C
        };

        // todo (etst) use different salt each time

        // use high iteration count to slow down decryption speed
        int iterationCount = 65536;

        // provide password, salt, iteration count for generating PBEKey of fixed-key-size PBE ciphers
        PBEKeySpec pbeKeySpec = new PBEKeySpec(passPhrase, salt, iterationCount);

        // create a secret (symmetric) key using PBE with MD5 and Triple DES
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndTripleDES");
        SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

        // construct a parameter set for password-based encryption as defined in the PKCS #5 standard
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, iterationCount);

        // initialize the ciphers with the given key
        Cipher pbeEcipher = Cipher.getInstance(pbeKey.getAlgorithm());
        pbeEcipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

        Cipher pbeDcipher = Cipher.getInstance(pbeKey.getAlgorithm());
        pbeDcipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);

        return new Encryption(pbeEcipher, pbeDcipher);
    }

}
