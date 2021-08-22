package nu.studer.gradle.credentials.domain

import nu.studer.java.util.OrderedProperties

/**
 * Transiently retrieves and adds credentials.
 */
final class CredentialsContainer {

    private final CredentialsEncryptor credentialsEncryptor
    private final OrderedProperties credentials

    CredentialsContainer(CredentialsEncryptor credentialsEncryptor, OrderedProperties initialCredentials) {
        this.credentialsEncryptor = credentialsEncryptor
        this.credentials = OrderedProperties.copyOf(initialCredentials)
    }

    String forKey(String name) {
        if (credentials.containsProperty(name)) {
            credentialsEncryptor.decrypt(credentials.getProperty(name))
        } else {
            null
        }
    }

}
