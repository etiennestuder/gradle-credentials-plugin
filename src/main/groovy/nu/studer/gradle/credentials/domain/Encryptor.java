package nu.studer.gradle.credentials.domain;

public interface Encryptor {
    public String encrypt(String value);
    public String decrypt(String encrypted);
}
