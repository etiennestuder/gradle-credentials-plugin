package nu.studer.gradle.credentials;

import nu.studer.gradle.credentials.domain.CredentialsContainer;
import nu.studer.gradle.credentials.domain.CredentialsEncryptor;
import nu.studer.gradle.credentials.domain.CredentialsPersistenceManager;
import nu.studer.gradle.util.AlwaysFalseSpec;
import nu.studer.gradle.util.MD5;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.util.GradleVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.function.Function;

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

    public static final String GROUP = "Credentials";

    private static final Action<Pair> NOOP = (Pair p) -> {
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsPlugin.class);

    @SuppressWarnings("NullableProblems")
    @Override
    public void apply(ExtensionAware extensionAware) {
        // abort if old Gradle version is not supported
        if (GradleVersion.current().getBaseVersion().compareTo(GradleVersion.version("5.0")) < 0) {
            throw new IllegalStateException("This version of the credentials plugin is not compatible with Gradle < 5.0");
        }

        // handle plugin application to settings file and project file
        if (extensionAware instanceof Settings) {
            Settings settings = (Settings) extensionAware;
            init(settings.getGradle(), settings, (String loc) -> settings.getSettingsDir().toPath().resolve(loc).toFile(), NOOP);
        } else if (extensionAware instanceof Project) {
            Project project = (Project) extensionAware;
            init(project.getGradle(), project, project::file, (Pair creds) -> addTasks(creds, project.getTasks()));
        } else {
            throw new IllegalStateException("The credentials plugin can only be applied to Settings and Project instances");
        }
    }

    private void init(Gradle gradle, ExtensionAware extensionAware, Function<String, File> locationResolver, Action<Pair> customizations) {
        // get the passphrase from the project properties, otherwise use the default passphrase
        String passphrase = getStringProperty(CREDENTIALS_PASSPHRASE_PROPERTY, DEFAULT_PASSPHRASE, extensionAware);

        // derive the name of the credentials file from the passphrase
        String credentialsFileName = deriveFileNameFromPassphrase(passphrase);

        // create credentials encryptor for the given passphrase
        CredentialsEncryptor credentialsEncryptor = CredentialsEncryptor.withPassphrase(passphrase.toCharArray());

        // create a credentials persistence manager that operates on the credentials file, possibly located in a user-configured folder
        String credentialsLocation = getStringProperty(CREDENTIALS_LOCATION_PROPERTY, null, extensionAware);
        File credentialsLocationDir = credentialsLocation != null ? locationResolver.apply(credentialsLocation) : gradle.getGradleUserHomeDir();
        File credentialsFile = new File(credentialsLocationDir, credentialsFileName);
        CredentialsPersistenceManager credentialsPersistenceManager = new CredentialsPersistenceManager(credentialsFile);

        // add a new 'credentials' property and transiently store the persisted credentials for access in build scripts
        CredentialsContainer credentialsContainer = new CredentialsContainer(credentialsEncryptor, credentialsPersistenceManager.readCredentials());
        setProperty(CREDENTIALS_CONTAINER_PROPERTY, credentialsContainer, extensionAware);
        LOGGER.debug("Registered property '" + CREDENTIALS_CONTAINER_PROPERTY + "'");

        // allow further ExtensionAware-specific customization
        customizations.execute(new Pair(credentialsEncryptor, credentialsPersistenceManager));
    }

    private String getStringProperty(String key, String defaultValue, ExtensionAware extensionAware) {
        ExtraPropertiesExtension properties = extensionAware.getExtensions().getExtraProperties();
        return properties.has(key) ? (String) properties.get(key) : defaultValue;
    }

    private void setProperty(String key, Object value, ExtensionAware extensionAware) {
        ExtraPropertiesExtension properties = extensionAware.getExtensions().getExtraProperties();
        properties.set(key, value);
    }

    private void addTasks(Pair result, TaskContainer tasks) {
        CredentialsEncryptor credentialsEncryptor = result.credentialsEncryptor;
        CredentialsPersistenceManager credentialsPersistenceManager = result.credentialsPersistenceManager;

        // add a task instance that stores new credentials through the credentials persistence manager
        TaskProvider<AddCredentialsTask> addCredentialsTaskProvider = tasks.register(ADD_CREDENTIALS_TASK_NAME, AddCredentialsTask.class, credentialsEncryptor, credentialsPersistenceManager);
        LOGGER.debug(String.format("Registered task '%s'", addCredentialsTaskProvider.getName()));

        // add a task instance that removes some credentials through the credentials persistence manager
        TaskProvider<RemoveCredentialsTask> removeCredentialsProvider = tasks.register(REMOVE_CREDENTIALS_TASK_NAME, RemoveCredentialsTask.class, credentialsPersistenceManager);
        LOGGER.debug(String.format("Registered task '%s'", removeCredentialsProvider.getName()));
    }

    private String deriveFileNameFromPassphrase(String passphrase) {
        // derive the name of the file that contains the credentials from the given passphrase
        String credentialsFileName;
        if (passphrase.equals(DEFAULT_PASSPHRASE)) {
            credentialsFileName = DEFAULT_PASSPHRASE_CREDENTIALS_FILE;
            LOGGER.debug("No explicit passphrase provided. Using default credentials file name: " + credentialsFileName);
        } else {
            credentialsFileName = "gradle." + MD5.generateMD5Hash(passphrase) + ".encrypted.properties";
            LOGGER.debug("Custom passphrase provided. Using credentials file name: " + credentialsFileName);
        }
        return credentialsFileName;
    }

    private static final class Pair {

        private final CredentialsEncryptor credentialsEncryptor;
        private final CredentialsPersistenceManager credentialsPersistenceManager;

        private Pair(CredentialsEncryptor credentialsEncryptor, CredentialsPersistenceManager credentialsPersistenceManager) {
            this.credentialsEncryptor = credentialsEncryptor;
            this.credentialsPersistenceManager = credentialsPersistenceManager;
        }

    }

}
