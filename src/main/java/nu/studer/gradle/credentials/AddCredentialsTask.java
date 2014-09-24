package nu.studer.gradle.credentials;

import nu.studer.gradle.util.Encryption;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Adds/updates the given credentials, specified as project properties.
 */
public class AddCredentialsTask extends DefaultTask {

    public static final String CREDENTIALS_KEY_PROPERTY = "credentialsKey";
    public static final String CREDENTIALS_VALUE_PROPERTY = "credentialsValue";

    private static final Logger LOGGER = LoggerFactory.getLogger(AddCredentialsTask.class);

    private CredentialsManager credentialsManager;

    public void setCredentialsManager(CredentialsManager credentialsManager) {
        this.credentialsManager = credentialsManager;
    }

    @Input
    public String getCredentialsKey() {
        return getProjectProperty(CREDENTIALS_KEY_PROPERTY);
    }

    @Input
    public String getCredentialsValue() {
        return getProjectProperty(CREDENTIALS_VALUE_PROPERTY);
    }

    @OutputFile
    public File getEncryptedPropertiesFile() {
        return this.credentialsManager.getCredentialsFile();
    }

    @TaskAction
    void addCredentials() throws IOException {
        // get credentials key and value from the project properties
        String key = getCredentialsKey();
        String value = getCredentialsValue();

        char[] placeholderValue = new char[value.length()];
        Arrays.fill(placeholderValue, '*');
        LOGGER.debug(String.format("Add credentials with key: '%s', value: '%s'", key, new String(placeholderValue)));

        // read the current persisted credentials
        Properties credentials = this.credentialsManager.readCredentials();

        // encrypt value
        Encryption encryption = Encryption.createEncryption("Default pass phrase".toCharArray());
        String encryptedValue = encryption.encrypt(value);

        // update credentials
        credentials.setProperty(key, encryptedValue);

        // persist the updated credentials
        this.credentialsManager.storeCredentials(credentials);
    }

    private String getProjectProperty(String key) {
        return (String) getProject().getProperties().get(key);
    }

}
