package nu.studer.gradle.credentials;

import nu.studer.gradle.credentials.domain.CredentialsPersistenceManager;
import nu.studer.gradle.util.AlwaysFalseSpec;
import nu.studer.java.util.OrderedProperties;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;

/**
 * Removes the given credentials, specified as project properties.
 */
public class RemoveCredentialsTask extends DefaultTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveCredentialsTask.class);

    private final CredentialsPersistenceManager credentialsPersistenceManager;
    private String key;

    @Inject
    public RemoveCredentialsTask(CredentialsPersistenceManager credentialsPersistenceManager) {
        this.credentialsPersistenceManager = credentialsPersistenceManager;
        getOutputs().upToDateWhen(AlwaysFalseSpec.INSTANCE);
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
    void removeCredentials() {
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

    @Override
    public String getDescription() {
        return "Removes the credentials specified through the project property 'credentialsKey'.";
    }

    @Override
    public String getGroup() {
        return CredentialsPlugin.GROUP;
    }
}
