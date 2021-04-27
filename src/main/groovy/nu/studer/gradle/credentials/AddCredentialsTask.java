package nu.studer.gradle.credentials;

import nu.studer.gradle.credentials.domain.CredentialsEncryptor;
import nu.studer.gradle.credentials.domain.CredentialsPersistenceManager;
import nu.studer.gradle.util.AlwaysFalseSpec;
import nu.studer.java.util.OrderedProperties;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;

/**
 * Adds/updates the given credentials, specified as project properties.
 */
public class AddCredentialsTask extends DefaultTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddCredentialsTask.class);

    private final CredentialsEncryptor credentialsEncryptor;
    private final CredentialsPersistenceManager credentialsPersistenceManager;
    private String key;
    private String value;

    @Inject
    public AddCredentialsTask(CredentialsEncryptor credentialsEncryptor, CredentialsPersistenceManager credentialsPersistenceManager) {
        this.credentialsEncryptor = credentialsEncryptor;
        this.credentialsPersistenceManager = credentialsPersistenceManager;
        getOutputs().upToDateWhen(AlwaysFalseSpec.INSTANCE);
    }

    @Option(option = "key", description = "The credentials key.")
    public void setKey(String key) {
        this.key = key;
    }

    @Option(option = "value", description = "The credentials value.")
    public void setValue(String value) {
        this.value = value;
    }

    @Internal("Do not annotate as @Input to avoid the key being stored in the task artifact cache")
    public String getCredentialsKey() {
        return key != null ? key : getProjectProperty(CredentialsPlugin.CREDENTIALS_KEY_PROPERTY);
    }

    @Internal("Do not annotate as @Input to avoid the value being stored in the task artifact cache")
    public String getCredentialsValue() {
        return value != null ? value : getProjectProperty(CredentialsPlugin.CREDENTIALS_VALUE_PROPERTY);
    }

    @OutputFile
    public File getEncryptedPropertiesFile() {
        return credentialsPersistenceManager.getCredentialsFile();
    }

    @TaskAction
    void addCredentials() {
        // get credentials key and value from the command line or project properties
        String key = getCredentialsKey();
        if (key == null) {
            throw new IllegalArgumentException("Credentials key must not be null");
        }

        String value = getCredentialsValue();
        if (value == null) {
            throw new IllegalArgumentException("Credentials value must not be null");
        }

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

    @Override
    public String getDescription() {
        return "Adds the credentials specified through the project properties 'credentialsKey' and 'credentialsValue'.";
    }

    @Override
    public String getGroup() {
        return CredentialsPlugin.GROUP;
    }
}
