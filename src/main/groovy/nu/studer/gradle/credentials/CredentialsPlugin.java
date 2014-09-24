package nu.studer.gradle.credentials;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

/**
 * Plugin to store and access encrypted credentials.
 */
public class CredentialsPlugin implements Plugin<Project> {

    public static final String CREDENTIALS_STORAGE_PROPERTY_NAME = "credentials";
    public static final String ENCRYPTED_CREDENTIALS_FILE = "gradle.encrypted.properties";
    public static final String ADD_CREDENTIALS_TASK_NAME = "addCredentials";
    public static final String REMOVE_CREDENTIALS_TASK_NAME = "removeCredentials";

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsPlugin.class);

    @Override
    public void apply(Project project) {
        // create a credentials manager
        File gradleUserHomeDir = project.getGradle().getGradleUserHomeDir();
        File credentialsFile = new File(gradleUserHomeDir, ENCRYPTED_CREDENTIALS_FILE);
        CredentialsPersistenceManager credentialsPersistenceManager = new CredentialsPersistenceManager(credentialsFile);

        // add a new 'credentials' property and transiently store the persisted credentials for access in build scripts
        Properties persistedCredentials = credentialsPersistenceManager.readCredentials();
        CredentialsContainer credentialsContainer = new CredentialsContainer(persistedCredentials);
        project.getExtensions().getExtraProperties().set(CREDENTIALS_STORAGE_PROPERTY_NAME, credentialsContainer);
        LOGGER.debug("Registered property '" + CREDENTIALS_STORAGE_PROPERTY_NAME + "'");

        // add a task instance that stores the given credentials in the credentials file, in encrypted format
        AddCredentialsTask addCredentials = project.getTasks().create(ADD_CREDENTIALS_TASK_NAME, AddCredentialsTask.class);
        addCredentials.setDescription("Adds the credentials specified through the project properties 'credentialsKey' and 'credentialsValue'.");
        addCredentials.setGroup("Credentials");
        addCredentials.setCredentialsPersistenceManager(credentialsPersistenceManager);
        LOGGER.debug(String.format("Registered task '%s'", addCredentials.getName()));
    }

}
