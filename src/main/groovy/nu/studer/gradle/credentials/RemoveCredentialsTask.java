package nu.studer.gradle.credentials;

import nu.studer.gradle.credentials.domain.CredentialsPersistenceManager;
import nu.studer.java.util.OrderedProperties;
import org.gradle.api.DefaultTask;
import org.gradle.api.internal.tasks.options.Option;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Removes the given credentials, specified as project properties.
 */
public class RemoveCredentialsTask extends DefaultTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveCredentialsTask.class);

    private CredentialsPersistenceManager credentialsPersistenceManager;
    private String key;

    public void setCredentialsPersistenceManager(CredentialsPersistenceManager credentialsPersistenceManager) {
        this.credentialsPersistenceManager = credentialsPersistenceManager;
    }

    @Option(option = "key", description = "The credentials key.")
    public void setKey(String key) {
        this.key = key;
    }

    public String getCredentialsKey() {
        return key != null ? key : getProjectProperty(CredentialsPlugin.CREDENTIALS_KEY_PROPERTY);
    }

    @OutputFile
    public File getEncryptedPropertiesFile() {
        return credentialsPersistenceManager.getCredentialsFile();
    }

    @TaskAction
    void removeCredentials() throws IOException {
        // get credentials key from the project properties
        String key = getCredentialsKey();

        LOGGER.debug(String.format("Remove credentials with key: '%s'", key));

        // read the current persisted credentials
        OrderedProperties credentials = credentialsPersistenceManager.readCredentials();

        // remove the credentials with the given key
        credentials.removeProperty(key);

        // persist the updated credentials
        credentialsPersistenceManager.storeCredentials(credentials);
    }

    private String getProjectProperty(String key) {
        return (String) getProject().getProperties().get(key);
    }

}
