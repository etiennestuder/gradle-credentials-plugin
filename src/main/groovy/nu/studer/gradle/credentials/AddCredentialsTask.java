package nu.studer.gradle.credentials;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(AddCredentialsTask.class);

    private CredentialsEncryptor credentialsEncryptor;
    private CredentialsPersistenceManager credentialsPersistenceManager;

    public void setCredentialsEncryptor(CredentialsEncryptor credentialsEncryptor) {
        this.credentialsEncryptor = credentialsEncryptor;
    }

    public void setCredentialsPersistenceManager(CredentialsPersistenceManager credentialsPersistenceManager) {
        this.credentialsPersistenceManager = credentialsPersistenceManager;
    }

    @Input
    public String getCredentialsKey() {
        return getProjectProperty(CredentialsPlugin.CREDENTIALS_KEY_PROPERTY);
    }

    @Input
    public String getCredentialsValue() {
        return getProjectProperty(CredentialsPlugin.CREDENTIALS_VALUE_PROPERTY);
    }

    @OutputFile
    public File getEncryptedPropertiesFile() {
        return this.credentialsPersistenceManager.getCredentialsFile();
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
        Properties credentials = this.credentialsPersistenceManager.readCredentials();

        // encrypt value and update credentials
        String encryptedValue = this.credentialsEncryptor.encrypt(value);
        credentials.setProperty(key, encryptedValue);

        // persist the updated credentials
        this.credentialsPersistenceManager.storeCredentials(credentials);
    }

    private String getProjectProperty(String key) {
        return (String) getProject().getProperties().get(key);
    }

}
