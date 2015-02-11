package nu.studer.gradle.credentials;

import nu.studer.gradle.credentials.domain.CredentialsPersistenceManager;
import nu.studer.gradle.credentials.domain.Encryptor;
import nu.studer.java.util.OrderedProperties;
import org.gradle.api.DefaultTask;
import org.gradle.api.internal.tasks.options.Option;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Adds/updates the given credentials, specified as project properties.
 */
public class AddCredentialsTask extends DefaultTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddCredentialsTask.class);

    private Encryptor credentialsEncryptor;
    private CredentialsPersistenceManager credentialsPersistenceManager;
    private String key;
    private String value;

    public void setCredentialsEncryptor(Encryptor credentialsEncryptor) {
        this.credentialsEncryptor = credentialsEncryptor;
    }

    public void setCredentialsPersistenceManager(CredentialsPersistenceManager credentialsPersistenceManager) {
        this.credentialsPersistenceManager = credentialsPersistenceManager;
    }

    @Option(option = "key", description = "The credentials key.")
    public void setKey(String key) {
        this.key = key;
    }

    @Option(option = "value", description = "The credentials value.")
    public void setValue(String value) {
        this.value = value;
    }

    @Input
    public String getCredentialsKey() {
        return key != null ? key : getProjectProperty(CredentialsPlugin.CREDENTIALS_KEY_PROPERTY);
    }

    @Input
    public String getCredentialsValue() {
        return value != null ? value : getProjectProperty(CredentialsPlugin.CREDENTIALS_VALUE_PROPERTY);
    }

    @OutputFile
    public File getEncryptedPropertiesFile() {
        return credentialsPersistenceManager.getCredentialsFile();
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
        OrderedProperties credentials = credentialsPersistenceManager.readCredentials();

        // encrypt value and update credentials
        String encryptedValue = credentialsEncryptor.encrypt(value);
        credentials.setProperty(key, encryptedValue);

        // persist the updated credentials
        credentialsPersistenceManager.storeCredentials(credentials);
    }

    private String getProjectProperty(String key) {
        return (String) getProject().getProperties().get(key);
    }

}
