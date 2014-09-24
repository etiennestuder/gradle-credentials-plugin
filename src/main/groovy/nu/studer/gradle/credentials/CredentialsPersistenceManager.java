package nu.studer.gradle.credentials;

import org.gradle.util.GUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

/**
 * Manages the storage and retrieval of encrypted credentials.
 */
public final class CredentialsPersistenceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsPersistenceManager.class);

    private final File credentialsFile;

    public CredentialsPersistenceManager(File credentialsFile) {
        this.credentialsFile = credentialsFile;
    }

    public File getCredentialsFile() {
        return new File(credentialsFile.toURI());
    }

    public Properties readCredentials() {
        Properties properties = new Properties();

        // read the file with the encrypted credentials, if it already exists
        File file = getCredentialsFile();
        if (file.exists()) {
            LOGGER.debug("Read existing credentials file: " + file.getAbsolutePath());
            Properties existingProperties = GUtil.loadProperties(file);
            properties.putAll(existingProperties);
        } else {
            LOGGER.debug("Credentials file does not exist yet: " + file.getAbsolutePath());
        }

        return properties;
    }

    public void storeCredentials(Properties credentials) {
        // write the updated credentials
        File file = getCredentialsFile();
        LOGGER.debug("Write updated credentials file: " + file.getAbsolutePath());
        GUtil.saveProperties(credentials, file);
    }

}
