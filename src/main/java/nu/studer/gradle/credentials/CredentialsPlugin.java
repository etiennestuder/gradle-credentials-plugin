package nu.studer.gradle.credentials;

import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.util.GUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

/**
 * Plugin to store and access encrypted credentials.
 */
public class CredentialsPlugin implements Plugin<Project> {

    public static final String CREDENTIALS_EXTENSION_NAME = "credentials";
    public static final String ADD_CREDENTIALS_TASK_NAME = "addCredentials";
    public static final String REMOVE_CREDENTIALS_TASK_NAME = "removeCredentials";

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsPlugin.class);

    @Override
    public void apply(Project project) {
        // add a new 'credentials' extension (the actual we pass in is irrelevant since we do everything though the extra properties)
        ExtensionAware credentials = (ExtensionAware) project.getExtensions().create(CREDENTIALS_EXTENSION_NAME, CredentialsPlugin.class);
        LOGGER.debug("Registered extension '" + CREDENTIALS_EXTENSION_NAME + "'");

        File gradleUserHomeDir = project.getGradle().getGradleUserHomeDir();
        File file = new File(gradleUserHomeDir, AddCredentialsTask.ENCRYPTED_PROPERTIES_FILE);
        Properties properties = GUtil.loadProperties(file);

        ExtraPropertiesExtension extraProperties = credentials.getExtensions().getExtraProperties();
        for (String name : properties.stringPropertyNames()) {
            extraProperties.set(name, properties.getProperty(name));
        }

        // add a task instance that stores the given credentials in the credentials properties file, encrypted
        DefaultTask setCredential = project.getTasks().create(ADD_CREDENTIALS_TASK_NAME, AddCredentialsTask.class);
        setCredential.setDescription("Adds the credentials specified through the project properties 'credentialsKey' and 'credentialsValue'.");
        setCredential.setGroup("Credentials");
        LOGGER.debug(String.format("Registered task '%s'", setCredential.getName()));
    }

}
