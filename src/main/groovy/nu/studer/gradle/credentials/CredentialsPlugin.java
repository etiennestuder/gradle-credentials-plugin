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
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.tasks.TaskContainer;
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

    public static final String DEFAULT_PASSPHRASE_CREDENTIALS_FILE = "gradle.%sencrypted.properties";
    public static final String DEFAULT_PASSPHRASE = ">>Default passphrase to encrypt passwords!<<";

    public static final String CREDENTIALS_CONTAINER_PROPERTY = "credentials";

    public static final String CREDENTIALS_LOCATION_PROPERTY = "credentialsLocation";
    public static final String CREDENTIALS_PASSPHRASE_PROPERTY = "credentialsPassphrase";
    public static final String CREDENTIALS_ENV_PROPERTY = "credentialsEnv";
    public static final String CREDENTIALS_KEY_PROPERTY = "credentialsKey";
    public static final String CREDENTIALS_VALUE_PROPERTY = "credentialsValue";

    public static final String ADD_CREDENTIALS_TASK_NAME = "addCredentials";
    public static final String REMOVE_CREDENTIALS_TASK_NAME = "removeCredentials";
    public static final String SHOW_CREDENTIALS_TASK_NAME = "showCredentials";

    private static final Action<Pair> NOOP = (Pair p) -> {
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsPlugin.class);

    public static String deriveFileNameFromPassphraseAndEnv(String env, String passphrase) {
        // derive the name of the file that contains the credentials from the given passphrase
        String credentialsFileName;
        if (isEmpty(passphrase)) {
            passphrase = DEFAULT_PASSPHRASE;
        }
        if (isEmpty(env)) {
            env = "";
        }
        String envPassphrase = env + "|" + passphrase;
        if (passphrase.equals(CredentialsPlugin.DEFAULT_PASSPHRASE) && isEmpty(env)) {
            credentialsFileName = String.format(CredentialsPlugin.DEFAULT_PASSPHRASE_CREDENTIALS_FILE, "");
            LOGGER.debug("No explicit passphrase provided. Using default credentials file name: " + credentialsFileName);
        } else {
            credentialsFileName = String.format(CredentialsPlugin.DEFAULT_PASSPHRASE_CREDENTIALS_FILE, MD5.generateMD5Hash(envPassphrase) + ".");
            LOGGER.debug("Custom passphrase provided. Using credentials file name: " + credentialsFileName);
        }
        return credentialsFileName;
    }

    private static boolean isEmpty(String string) {
        return string == null || string.trim().length() == 0;
    }

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
            init(settings, (String loc) -> loc != null ? settings.getSettingsDir().toPath().resolve(loc).toFile() : settings.getGradle().getGradleUserHomeDir(), NOOP);
        } else if (extensionAware instanceof Project) {
            Project project = (Project) extensionAware;
            init(project, (String loc) -> loc != null ? project.file(loc) : project.getGradle().getGradleUserHomeDir(), (Pair creds) -> addTasks(creds, project.getTasks()));
        } else {
            throw new IllegalStateException("The credentials plugin can only be applied to Settings and Project instances");
        }
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
        AddCredentialsTask addCredentials = tasks.create(ADD_CREDENTIALS_TASK_NAME, AddCredentialsTask.class);
        addCredentials.setDescription("Adds the credentials specified through the project properties 'credentialsKey' and 'credentialsValue'.");
        addCredentials.setGroup("Credentials");
        addCredentials.setCredentialsEncryptor(credentialsEncryptor);
        addCredentials.setCredentialsPersistenceManager(credentialsPersistenceManager);
        addCredentials.getOutputs().upToDateWhen(AlwaysFalseSpec.INSTANCE);
        LOGGER.debug(String.format("Registered task '%s'", addCredentials.getName()));

        // add a task instance that removes some credentials through the credentials persistence manager
        RemoveCredentialsTask removeCredentials = tasks.create(REMOVE_CREDENTIALS_TASK_NAME, RemoveCredentialsTask.class);
        removeCredentials.setDescription("Removes the credentials specified through the project property 'credentialsKey'.");
        removeCredentials.setGroup("Credentials");
        removeCredentials.setCredentialsPersistenceManager(credentialsPersistenceManager);
        removeCredentials.getOutputs().upToDateWhen(AlwaysFalseSpec.INSTANCE);
        LOGGER.debug(String.format("Registered task '%s'", removeCredentials.getName()));

        ShowCredentialsTask showCredentialsTask = tasks.create(SHOW_CREDENTIALS_TASK_NAME, ShowCredentialsTask.class);
        showCredentialsTask.setDescription("Shows the current credentials for the specified 'credentialsEnv' (or --env) and 'credentialsKey' (or --key) and optional 'credentialsPassphrase' (or --pass) and optional 'credentialsLocation' (or --loc)");
        showCredentialsTask.setGroup("Credentials");
        showCredentialsTask.setCredentialsEncryptor(credentialsEncryptor);
        showCredentialsTask.setCredentialsPersistenceManager(credentialsPersistenceManager);
        showCredentialsTask.getOutputs().upToDateWhen(AlwaysFalseSpec.INSTANCE);
        LOGGER.debug(String.format("Registered task '%s'", removeCredentials.getName()));
    }

    private void init(ExtensionAware extensionAware, Function<String, File> locationResolver, Action<Pair> customizations) {
        // get the passphrase, env, and init vector from the project properties, otherwise use the default passphrase
        String passphrase = getStringProperty(CREDENTIALS_PASSPHRASE_PROPERTY, DEFAULT_PASSPHRASE, extensionAware);

        String location = getStringProperty(CREDENTIALS_LOCATION_PROPERTY, null, extensionAware);
        String env = getStringProperty(CREDENTIALS_ENV_PROPERTY, "", extensionAware);

        // create credentials encryptor for the given passphrase
        CredentialsEncryptor credentialsEncryptor = CredentialsEncryptor.withPassphrase(passphrase.toCharArray());

        // create a credentials persistence manager that operates on the credentials file, possibly located in a user-configured folder
        CredentialsPersistenceManager credentialsPersistenceManager = new CredentialsPersistenceManager(deriveFileNameFromPassphraseAndEnv(env, passphrase), location, extensionAware, locationResolver);

        // add a new 'credentials' property and transiently store the persisted credentials for access in build scripts
        CredentialsContainer credentialsContainer = new CredentialsContainer(credentialsEncryptor, credentialsPersistenceManager.readCredentials());
        setProperty(CREDENTIALS_CONTAINER_PROPERTY, credentialsContainer, extensionAware);
        LOGGER.debug("Registered property '" + CREDENTIALS_CONTAINER_PROPERTY + "'");

        // allow further ExtensionAware-specific customization
        customizations.execute(new Pair(credentialsEncryptor, credentialsPersistenceManager));
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
