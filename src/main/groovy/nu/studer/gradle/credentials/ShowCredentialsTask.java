package nu.studer.gradle.credentials;

import nu.studer.gradle.credentials.domain.CredentialsContainer;
import nu.studer.gradle.credentials.domain.CredentialsEncryptor;
import nu.studer.gradle.credentials.domain.CredentialsPersistenceManager;
import nu.studer.java.util.OrderedProperties;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Adds/updates the given credentials, specified as project properties.
 */
public class ShowCredentialsTask extends DefaultTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShowCredentialsTask.class);

    private CredentialsEncryptor credentialsEncryptor;
    private CredentialsPersistenceManager credentialsPersistenceManager;
    private String env;
    private String loc;
    private String pass;
    private String key;

    public void setCredentialsEncryptor(CredentialsEncryptor credentialsEncryptor) {
        this.credentialsEncryptor = credentialsEncryptor;
    }

    public void setCredentialsPersistenceManager(CredentialsPersistenceManager credentialsPersistenceManager) {
        this.credentialsPersistenceManager = credentialsPersistenceManager;
    }

    @Option(option = "env", description = "The credentials env.")
    public void setEnv(String env) {
        this.env = env;
    }

    @Option(option = "loc", description = "The credentials location.")
    public void setLoc(String loc) {
        this.loc = loc;
    }

    @Option(option = "pass", description = "The credentials pass.")
    public void setPass(String pass) {
        this.pass = pass;
    }

    @Option(option = "key", description = "The credentials key.")
    public void setKey(String key) {
        this.key = key;
    }

    @Internal("Do not annotate as @Input to avoid the env being stored in the task artifact cache")
    public String getCredentialsEnv() {
        return env != null ? env : getProjectProperty(CredentialsPlugin.CREDENTIALS_ENV_PROPERTY);
    }

    @Internal("Do not annotate as @Input to avoid the loc being stored in the task artifact cache")
    public String getCredentialsLoc() {
        return loc != null ? loc : getProjectProperty(CredentialsPlugin.CREDENTIALS_LOCATION_PROPERTY);
    }

    @Internal("Do not annotate as @Input to avoid the pass being stored in the task artifact cache")
    public String getCredentialsPass() {
        return pass != null ? pass : getProjectProperty(CredentialsPlugin.CREDENTIALS_PASSPHRASE_PROPERTY) != null ? getProjectProperty(CredentialsPlugin.CREDENTIALS_PASSPHRASE_PROPERTY) : CredentialsPlugin.DEFAULT_PASSPHRASE;
    }

    @Internal("Do not annotate as @Input to avoid the key being stored in the task artifact cache")
    public String getCredentialsKey() {
        return key != null ? key : getProjectProperty(CredentialsPlugin.CREDENTIALS_KEY_PROPERTY);
    }

    @OutputFile
    public File getEncryptedPropertiesFile() {
        return credentialsPersistenceManager.getCredentialsFile();
    }

    @TaskAction
    void showCredentials() {
        String env = getCredentialsEnv();
        String loc = getCredentialsLoc();
        String pass = getCredentialsPass();
        // get credentials key and value from the command line or project properties
        String key = getCredentialsKey();
        if (key == null) {
            throw new IllegalArgumentException("Credentials key must not be null");
        }

        if (env != null || pass != null || loc != null) {
            credentialsPersistenceManager = CredentialsPersistenceManager.fromCredentialsPersistenceManager(CredentialsPlugin.deriveFileNameFromPassphraseAndEnv(env, pass), loc, credentialsPersistenceManager);
            if (pass != null) {
                credentialsEncryptor = CredentialsEncryptor.withPassphrase(pass.toCharArray());
            }
            updateCredentials();
        }
        LOGGER.info("ShowCredentials: env: '{}' key: '{}' pass: '{}'", env, key, pass);

        // read the current persisted credentials
        OrderedProperties credentials = credentialsPersistenceManager.readCredentials();

        // decrypt value and show credentials
        String decryptedValue = credentialsEncryptor.decrypt(credentials.getProperty(key));
        if (decryptedValue == null) {
            String hashedKey = credentialsEncryptor.hash(key);
            decryptedValue = credentialsEncryptor.decrypt(credentials.getProperty(hashedKey));
            LOGGER.info("ShowCredentials(HASHED): env: '{}' key: '{}->{}', value: '{}'", env, key, hashedKey, decryptedValue);
        } else {
            LOGGER.info("ShowCredentials(NOT HASHED): env: '{}' key: '{}->{}', value: '{}'", env, key, key, decryptedValue);
        }
    }

    private void updateCredentials() {
        ExtraPropertiesExtension properties = credentialsPersistenceManager.getExtensionAware().getExtensions().getExtraProperties();
        CredentialsContainer credentialsContainer = new CredentialsContainer(credentialsEncryptor, credentialsPersistenceManager.readCredentials());
        properties.set(CredentialsPlugin.CREDENTIALS_CONTAINER_PROPERTY, credentialsContainer);

    }

    private String getProjectProperty(String key) {
        return (String) getProject().getProperties().get(key);
    }

}
