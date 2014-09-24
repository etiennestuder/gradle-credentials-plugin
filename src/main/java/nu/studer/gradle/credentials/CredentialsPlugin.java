package nu.studer.gradle.credentials;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

/**
 * Plugin to store and access encrypted credentials.
 */
public class CredentialsPlugin implements Plugin<Project> {

    public static final String CREDENTIALS_EXTENSION_NAME = "credentials";
    public static final String ENCRYPTED_CREDENTIALS_FILE = "gradle.encrypted.properties";
    public static final String ADD_CREDENTIALS_TASK_NAME = "addCredentials";
    public static final String REMOVE_CREDENTIALS_TASK_NAME = "removeCredentials";

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsPlugin.class);

    @Override
    public void apply(Project project) {
        // create a credentials manager
        File gradleUserHomeDir = project.getGradle().getGradleUserHomeDir();
        File credentialsFile = new File(gradleUserHomeDir, ENCRYPTED_CREDENTIALS_FILE);
        CredentialsManager credentialsManager = new CredentialsManager(credentialsFile);

        // read the credentials already stored in the credentials file
        Properties credentials = credentialsManager.readCredentials();

        // add a new 'credentials' extension (the actual class we pass in is irrelevant since we do everything through the extra properties mechanism)
        ExtensionAware credentialsExtension = (ExtensionAware) project.getExtensions().create(CREDENTIALS_EXTENSION_NAME, CredentialsPlugin.class);
        LOGGER.debug("Registered extension '" + CREDENTIALS_EXTENSION_NAME + "'");

        // store the current credentials as extra properties for easy access in build scripts
        ExtraPropertiesExtension extraProperties = credentialsExtension.getExtensions().getExtraProperties();
        for (String name : credentials.stringPropertyNames()) {
            extraProperties.set(name, credentials.getProperty(name));
        }

        // add a task instance that stores the given credentials in the credentials file, in encrypted format
        AddCredentialsTask addCredentials = project.getTasks().create(ADD_CREDENTIALS_TASK_NAME, AddCredentialsTask.class);
        addCredentials.setDescription("Adds the credentials specified through the project properties 'credentialsKey' and 'credentialsValue'.");
        addCredentials.setGroup("Credentials");
        addCredentials.setCredentialsManager(credentialsManager);
        LOGGER.debug(String.format("Registered task '%s'", addCredentials.getName()));
    }

}
