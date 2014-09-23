package nu.studer.gradle.credentials;

import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin to store and access encrypted credentials.
 */
public class CredentialsPlugin implements Plugin<Project> {

    public static final String ADD_CREDENTIALS_TASK_NAME = "addCredentials";
    public static final String REMOVE_CREDENTIALS_TASK_NAME = "removeCredentials";

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsPlugin.class);

    @Override
    public void apply(Project project) {
        // add a task instance that stores the given credentials in the credentials properties file, encrypted
        DefaultTask setCredential = project.getTasks().create(ADD_CREDENTIALS_TASK_NAME, AddCredentialsTask.class);
        setCredential.setDescription("Adds the credentials specified through the project properties 'credentialsKey' and 'credentialsValue'.");
        setCredential.setGroup("Credentials");
        LOGGER.debug(String.format("Registered task '%s'", setCredential.getName()));
    }

}
