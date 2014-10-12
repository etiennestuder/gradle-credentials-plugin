package nu.studer.gradle.credentials.domain;

import nu.studer.java.util.OrderedProperties;
import org.gradle.api.UncheckedIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

    public OrderedProperties readCredentials() {
        // read the file with the encrypted credentials, if it already exists
        File file = getCredentialsFile();
        if (file.exists()) {
            LOGGER.debug("Read existing credentials file: " + file.getAbsolutePath());
            return loadProperties(file);
        } else {
            LOGGER.debug("Credentials file does not exist yet: " + file.getAbsolutePath());
            return new OrderedProperties();
        }
    }

    public void storeCredentials(OrderedProperties credentials) {
        // write the updated credentials
        File file = getCredentialsFile();
        LOGGER.debug("Write updated credentials file: " + file.getAbsolutePath());
        saveProperties(credentials, file);
    }

    private static OrderedProperties loadProperties(File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            try {
                return loadProperties(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static OrderedProperties loadProperties(InputStream inputStream) {
        OrderedProperties properties = new OrderedProperties();
        try {
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return properties;
    }

    private static void saveProperties(OrderedProperties properties, File file) {
        try {
            FileOutputStream propertiesFileOutputStream = new FileOutputStream(file);
            try {
                properties.store(propertiesFileOutputStream, null);
            } finally {
                propertiesFileOutputStream.close();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
