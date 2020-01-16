package nu.studer.gradle.credentials.domain;

import nu.studer.java.util.OrderedProperties;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.plugins.ExtensionAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.function.Function;

/**
 * Manages the storage and retrieval of encrypted credentials.
 */
public final class CredentialsPersistenceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsPersistenceManager.class);

    private final File credentialsFile;
    private final ExtensionAware extensionAware;
    private final Function<String, File> locationResolver;

    public CredentialsPersistenceManager(String credentialsFileName, String location, ExtensionAware extensionAware, Function<String, File> locationResolver) {
        this.extensionAware = extensionAware;
        this.locationResolver = locationResolver;
        File credentialsLocationDir = locationResolver.apply(location);
        this.credentialsFile = new File(credentialsLocationDir, credentialsFileName);
    }

    public static CredentialsPersistenceManager fromCredentialsPersistenceManager(String credentialsFileName, String loc, CredentialsPersistenceManager originalManager) {
        return new CredentialsPersistenceManager(credentialsFileName, loc, originalManager.extensionAware, originalManager.locationResolver);
    }

    private static void loadProperties(OrderedProperties properties, File file) {
        try {
            try (FileInputStream inputStream = new FileInputStream(file)) {
                loadProperties(properties, inputStream);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ExtensionAware getExtensionAware() {
        return this.extensionAware;
    }

    public OrderedProperties readCredentials() {
        OrderedProperties credentials = createOrderedProperties();

        // read the file with the encrypted credentials, if it already exists
        File file = getCredentialsFile();
        if (file.exists()) {
            LOGGER.debug("Read existing credentials file: " + file.getAbsolutePath());
            loadProperties(credentials, file);
        } else {
            LOGGER.debug("Credentials file does not exist yet: " + file.getAbsolutePath());
        }

        return credentials;
    }

    public void storeCredentials(OrderedProperties credentials) {
        // write the updated credentials
        File file = getCredentialsFile();
        LOGGER.debug("Write updated credentials file: " + file.getAbsolutePath());
        saveProperties(credentials, file);
    }

    public File getCredentialsFile() {
        return this.credentialsFile;
    }

    private static void loadProperties(OrderedProperties properties, InputStream stream) {
        try {
            properties.load(stream);
            stream.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void saveProperties(OrderedProperties properties, File file) {
        try {
            try (FileOutputStream propertiesFileOutputStream = new FileOutputStream(file)) {
                properties.store(propertiesFileOutputStream, null);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static OrderedProperties createOrderedProperties() {
        return new OrderedProperties.OrderedPropertiesBuilder().
                withSuppressDateInComment(true).
                build();
    }

}
