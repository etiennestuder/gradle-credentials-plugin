package nu.studer.gradle.credentials;

import nu.studer.gradle.util.Encryption;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.GUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Adds/updates the given credentials, specified as project properties.
 */
public class AddCredentialsTask extends DefaultTask {

    public static final String CREDENTIALS_KEY_PROPERTY = "credentialsKey";
    public static final String CREDENTIALS_VALUE_PROPERTY = "credentialsValue";
    public static final String ENCRYPTED_PROPERTIES_FILE = "gradle.encrypted.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(AddCredentialsTask.class);

    @Input
    public String getCredentialsKey() {
        return getProjectProperty(CREDENTIALS_KEY_PROPERTY);
    }

    @Input
    public String getCredentialsValue() {
        return getProjectProperty(CREDENTIALS_VALUE_PROPERTY);
    }

    @OutputFile
    public File getEncryptedPropertiesFile() {
        File gradleUserHomeDir = getProject().getGradle().getGradleUserHomeDir();
        return new File(gradleUserHomeDir, ENCRYPTED_PROPERTIES_FILE);
    }

    @TaskAction
    void addCredentials() throws IOException {
        // get credentials key and value from the project properties
        String key = getCredentialsKey();
        String value = getCredentialsValue();

        char[] placeholderValue = new char[value.length()];
        Arrays.fill(placeholderValue, '*');
        LOGGER.debug(String.format("Add credentials with key: '%s', value: '%s'", key, new String(placeholderValue)));

        // manage read/write/update of credentials through the Java Properties mechanism
        Properties properties = new Properties();

        // read the file with the encrypted credentials, if it already exists
        File file = getEncryptedPropertiesFile();
        if (file.exists()) {
            LOGGER.debug("Read existing credentials file: " + file.getAbsolutePath());
            Properties existingProperties = GUtil.loadProperties(file);
            properties.putAll(existingProperties);
        } else {
            LOGGER.debug("Credentials file does not exist yet: " + file.getAbsolutePath());
        }

        // encrypt value
        Encryption encryption = Encryption.createEncryption("Default pass phrase".toCharArray());
        String encryptedValue = encryption.encrypt(value);

        // update credentials
        properties.setProperty(key, encryptedValue);

        // write the updated credentials
        LOGGER.debug("Write updated credentials file: " + file.getAbsolutePath());
        GUtil.saveProperties(properties, file);
    }

    private String getProjectProperty(String key) {
        return (String) getProject().getProperties().get(key);
    }

}
