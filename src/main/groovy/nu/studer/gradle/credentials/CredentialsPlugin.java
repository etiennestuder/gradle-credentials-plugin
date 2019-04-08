package nu.studer.gradle.credentials;

import nu.studer.gradle.credentials.domain.CredentialsContainer;
import nu.studer.gradle.credentials.domain.CredentialsEncryptor;
import nu.studer.gradle.credentials.domain.CredentialsPersistenceManager;
import nu.studer.gradle.util.AlwaysFalseSpec;
import nu.studer.gradle.util.MD5;
import nu.studer.java.util.OrderedProperties;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.tasks.TaskContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Plugin to store and access encrypted credentials using password-based encryption (PBE). The credentials are stored in the Gradle home directory in a separate file for each
 * passphrase. If no passphrase is provided, a default passphrase is used and the credentials are stored in the default credentials file 'gradle.encrypted.properties'. While
 * running a build, only one passphrase is active per project.
 * <p>
 * The plugin provides a credentials container through the 'credentials' property that is available from the Gradle project. This allows access to credentials in the form of
 * <code>project.myCredentialKey</code>. The already persisted credentials can be accessed through the credentials container, and new credentials can be added to the container
 * ad-hoc while the build is executed. Credentials added ad-hoc are not available beyond the lifetime of the build.
 * <p>
 * The plugin adds a task to add credentials and a task to remove credentials.
 */
public class CredentialsPlugin implements Plugin<ExtensionAware> {

    public static final String DEFAULT_PASSPHRASE_CREDENTIALS_FILE = "gradle.encrypted.properties";
    public static final String DEFAULT_PASSPHRASE = ">>Default passphrase to encrypt passwords!<<";

    public static final String CREDENTIALS_CONTAINER_PROPERTY = "credentials";

    public static final String CREDENTIALS_LOCATION_PROPERTY = "credentialsLocation";
    public static final String CREDENTIALS_PASSPHRASE_PROPERTY = "credentialsPassphrase";
    public static final String CREDENTIALS_KEY_PROPERTY = "credentialsKey";
    public static final String CREDENTIALS_VALUE_PROPERTY = "credentialsValue";

    public static final String ADD_CREDENTIALS_TASK_NAME = "addCredentials";
    public static final String REMOVE_CREDENTIALS_TASK_NAME = "removeCredentials";

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsPlugin.class);

    @Override
    public void apply(ExtensionAware target) {
        Applier applier;

        if (target instanceof Project) {
            applier = new ProjectApplier((Project) target);
        } else {
            if (target instanceof Settings) {
                applier = new SettingsApplier((Settings) target);
            } else {
                throw new GradleException("The specified target has an unsupported type: " +
                        target.getClass().getName());
            }
        }

        applier.apply();
    }

    private static abstract class Applier<T extends ExtensionAware> {
        protected final T target;

        protected final CredentialsEncryptor credentialsEncryptor;
        protected final CredentialsPersistenceManager credentialsPersistenceManager;

        protected Applier(T target) {
            this.target = target;
            // get the passphrase from the project properties, otherwise use the default passphrase
            String passphrase = getProperty(CREDENTIALS_PASSPHRASE_PROPERTY, DEFAULT_PASSPHRASE);

            // derive the name of the credentials file from the passphrase
            String credentialsFileName = deriveFileNameFromPassphrase(passphrase);

            // create credentials encryptor for the given passphrase
            this.credentialsEncryptor = CredentialsEncryptor.withPassphrase(passphrase.toCharArray());

            // create a credentials persistence manager that operates on the credentials file, possibly located in a
            // user-configured folder
            String customCredentialsLocation = getProperty(CREDENTIALS_LOCATION_PROPERTY, null);
            File credentialsLocationDir = this.getCredentialsDirectory(customCredentialsLocation);
            File credentialsFile = new File(credentialsLocationDir, credentialsFileName);
            this.credentialsPersistenceManager = new CredentialsPersistenceManager(credentialsFile);
        }

        void apply() {
            OrderedProperties credentials = this.credentialsPersistenceManager.readCredentials();
            // add a new 'credentials' property and transiently store the persisted credentials for access in build scripts
            CredentialsContainer credentialsContainer =
                    new CredentialsContainer(this.credentialsEncryptor, credentials);
            this.target.getExtensions().getExtraProperties().set(CREDENTIALS_CONTAINER_PROPERTY, credentialsContainer);
            LOGGER.debug("Registered property '" + CREDENTIALS_CONTAINER_PROPERTY + "'");
        }

        private String getProperty(String key, String defaultValue) {
            ExtraPropertiesExtension properties = this.target.getExtensions().getExtraProperties();

            return properties.has(key) ? (String) properties.get(key) : defaultValue;
        }

        protected abstract File getCredentialsDirectory(String customCredentialsLocation);

        private String deriveFileNameFromPassphrase(String passphrase) {
            // derive the name of the file that contains the credentials from the given passphrase
            String credentialsFileName;
            if (passphrase.equals(DEFAULT_PASSPHRASE)) {
                credentialsFileName = DEFAULT_PASSPHRASE_CREDENTIALS_FILE;
                LOGGER.debug(
                        "No explicit passphrase provided. Using default credentials file name: " + credentialsFileName);
            } else {
                credentialsFileName = "gradle." + MD5.generateMD5Hash(passphrase) + ".encrypted.properties";
                LOGGER.debug("Custom passphrase provided. Using credentials file name: " + credentialsFileName);
            }
            return credentialsFileName;
        }
    }

    private static final class ProjectApplier extends Applier<Project> {
        protected ProjectApplier(Project target) {
            super(target);
        }

        @Override
        void apply() {
            super.apply();

            TaskContainer taskContainer = this.target.getTasks();

            // add a task instance that stores new credentials through the credentials persistence manager
            AddCredentialsTask addCredentials =
                    taskContainer.create(ADD_CREDENTIALS_TASK_NAME, AddCredentialsTask.class);
            addCredentials.setDescription("Adds the credentials specified through the project properties "
                    + "'credentialsKey' and 'credentialsValue'.");
            addCredentials.setGroup("Credentials");
            addCredentials.setCredentialsEncryptor(this.credentialsEncryptor);
            addCredentials.setCredentialsPersistenceManager(this.credentialsPersistenceManager);
            addCredentials.getOutputs().upToDateWhen(AlwaysFalseSpec.INSTANCE);
            LOGGER.debug(String.format("Registered task '%s'", addCredentials.getName()));

            // add a task instance that removes some credentials through the credentials persistence manager
            RemoveCredentialsTask removeCredentials =
                    taskContainer.create(REMOVE_CREDENTIALS_TASK_NAME, RemoveCredentialsTask.class);
            removeCredentials
                    .setDescription("Removes the credentials specified through the project property 'credentialsKey'.");
            removeCredentials.setGroup("Credentials");
            removeCredentials.setCredentialsPersistenceManager(this.credentialsPersistenceManager);
            removeCredentials.getOutputs().upToDateWhen(AlwaysFalseSpec.INSTANCE);
            LOGGER.debug(String.format("Registered task '%s'", removeCredentials.getName()));
        }

        @Override
        protected File getCredentialsDirectory(String customCredentialsLocation) {
            return customCredentialsLocation == null ?
                    this.target.getGradle().getGradleUserHomeDir() : this.target.file(customCredentialsLocation);
        }
    }

    private static final class SettingsApplier extends Applier<Settings> {
        protected SettingsApplier(Settings target) {
            super(target);
        }

        @Override
        protected File getCredentialsDirectory(String customCredentialsLocation) {
            if (customCredentialsLocation == null) {
                return this.target.getGradle().getGradleUserHomeDir();
            } else {
                return this.target.getSettingsDir().toPath().resolve(customCredentialsLocation).toFile();
            }
        }
    }
}
