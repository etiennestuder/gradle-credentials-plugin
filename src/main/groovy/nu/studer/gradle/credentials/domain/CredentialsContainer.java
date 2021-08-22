package nu.studer.gradle.credentials.domain;

import nu.studer.java.util.OrderedProperties;

/**
 * Transiently retrieves and adds credentials.
 */
public final class CredentialsContainer {

    private final CredentialsEncryptor credentialsEncryptor;
    private final OrderedProperties credentials;

    public CredentialsContainer(CredentialsEncryptor credentialsEncryptor, OrderedProperties initialCredentials) {
        this.credentialsEncryptor = credentialsEncryptor;
        this.credentials = OrderedProperties.copyOf(initialCredentials);
    }

    public String forKey(String name) {
        if (credentials.containsProperty(name)) {
            return credentialsEncryptor.decrypt(credentials.getProperty(name));
        } else {
            return null;
        }

    }

}
