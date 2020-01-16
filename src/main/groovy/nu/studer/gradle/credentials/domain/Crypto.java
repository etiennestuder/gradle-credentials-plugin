package nu.studer.gradle.credentials.domain;

import nu.studer.gradle.util.Base64;
import nu.studer.gradle.util.MD5;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class Crypto {
    private static final String AES_CBC_PKCS_5_PADDING = "AES/CBC/PKCS5Padding";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 128;
    private static final String PBKDF_2_WITH_HMAC_SHA_1 = "PBKDF2WithHmacSHA1";
    private static final String AES = "AES";

    private static final byte[] OLD_BAD_SALT = {
            (byte) 0x1F, (byte) 0x13, (byte) 0xE5, (byte) 0xB2,
            (byte) 0x49, (byte) 0x2C, (byte) 0xC3, (byte) 0x3C
    };

    private Crypto() {
        throw new IllegalStateException("Not to be instantiated");
    }

    public static Crypto.Encryption createEncryption(@Nonnull final char[] passphrase) {
        try {
            final SecureRandom random = SecureRandom.getInstanceStrong();
            return new Crypto.Encryption() {
                @Override
                public String encrypt(String clearText) {
                    try {
                        return _encrypt(passphrase, clearText);
                    } catch (Exception e) {
                        return "";
                    }
                }

                private String _encrypt(char[] key, String clearText) throws Exception {
                    byte[] salt = createSalt(random);

                    SecretKey pbeKey = getSecretKey(salt, key);
                    byte[] iv = createInitializationVector(random);
                    AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);

                    Cipher cipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);
                    cipher.init(Cipher.ENCRYPT_MODE, pbeKey, ivSpec);
                    byte[] clearTextUtf8 = clearText.getBytes(StandardCharsets.UTF_8);
                    byte[] payload = cipher.doFinal(clearTextUtf8);
                    payload = concat(payload, iv, salt);

                    return Base64.encodeBase64(payload);
                }

                // Lifted with <3 from google guava Bytes.concat()
                byte[] concat(byte[]... arrays) {
                    int length = 0;
                    for (byte[] array : arrays) {
                        length += array.length;
                    }
                    byte[] result = new byte[length];
                    int pos = 0;
                    for (byte[] array : arrays) {
                        System.arraycopy(array, 0, result, pos, array.length);
                        pos += array.length;
                    }
                    return result;
                }

                @Override
                public String decrypt(String encryptedBase64Text) {
                    try {
                        return _decrypt(passphrase, encryptedBase64Text);
                    } catch (Exception e) {
                        //Let's try the old way - for backwards compatibility
                        return backwardsCompatibility_decrypt(encryptedBase64Text);
                    }
                }

                private String _decrypt(char[] pass, String encryptedBase64Text) throws Exception {
                    byte[] concat = Base64.decodeBase64(encryptedBase64Text);
                    byte[] salt = new byte[8];
                    System.arraycopy(concat, concat.length - salt.length, salt, 0, salt.length);
                    Cipher cipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);
                    byte[] iv = new byte[cipher.getBlockSize()];
                    System.arraycopy(concat, concat.length - salt.length - iv.length, iv, 0, iv.length);
                    byte[] payload = new byte[concat.length - salt.length - iv.length];
                    System.arraycopy(concat, 0, payload, 0, payload.length);

                    SecretKey pbeKey = getSecretKey(salt, pass);
                    AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);

                    cipher.init(Cipher.DECRYPT_MODE, pbeKey, ivSpec);
                    byte[] decrypted = cipher.doFinal(payload);
                    return new String(decrypted, StandardCharsets.UTF_8);
                }

                @Override
                public String hash(String key) {
                    return MD5.generateMD5Hash(key + new String(passphrase));
                }

                private String backwardsCompatibility_decrypt(String encryptedBase64Text) {
                    try {
                        SecretKey pbeKey = getSecretKey(OLD_BAD_SALT, passphrase);
                        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);
                        byte[] iv = new byte[cipher.getBlockSize()];
                        for (int i = 0; i < iv.length; i++) {
                            iv[i] = (byte) i;
                        }
                        AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
                        cipher.init(Cipher.DECRYPT_MODE, pbeKey, ivSpec);
                        byte[] payload = Base64.decodeBase64(encryptedBase64Text);
                        byte[] decrypted = cipher.doFinal(payload);
                        return new String(decrypted, StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        //Got nothing for ya.
                        return "";
                    }
                }

                private SecretKey getSecretKey(byte[] salt, @Nonnull char[] passphrase) throws NoSuchAlgorithmException, InvalidKeySpecException {
                    KeySpec pbeKeySpec = new PBEKeySpec(passphrase, salt, ITERATIONS, KEY_LENGTH);

                    SecretKeyFactory keyFac = SecretKeyFactory.getInstance(PBKDF_2_WITH_HMAC_SHA_1);
                    SecretKey tmpKey = keyFac.generateSecret(pbeKeySpec);
                    return new SecretKeySpec(tmpKey.getEncoded(), AES);
                }
            };
        } catch (GeneralSecurityException gse) {
            throw new RuntimeException("Could not setup security", gse);
        }
    }

    private static byte[] createSalt(SecureRandom random) {
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        return salt;
    }

    private static byte[] createInitializationVector(SecureRandom random) throws GeneralSecurityException {
        int blockSize = Cipher.getInstance(AES_CBC_PKCS_5_PADDING).getBlockSize();
        byte[] iv = new byte[blockSize];
        random.nextBytes(iv);
        return iv;
    }

    public interface Encryption {
        String encrypt(String clearText);

        String decrypt(String encryptedBas64Text);

        String hash(String key);
    }
}
