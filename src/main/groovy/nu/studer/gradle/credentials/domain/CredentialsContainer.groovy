package nu.studer.gradle.credentials.domain

import nu.studer.java.util.OrderedProperties

/**
 * Transiently retrieves and adds credentials.
 */
class CredentialsContainer {
  private final Encryptor credentialsEncryptor
  private final OrderedProperties credentials

  CredentialsContainer(Encryptor encryptor, OrderedProperties initialCredentials) {
    this.credentialsEncryptor = encryptor
    this.credentials = OrderedProperties.copyOf(initialCredentials)
  }

  def propertyMissing(String name) {
    if (credentials.containsProperty(name)) {
      credentialsEncryptor.decrypt(credentials.getProperty(name))
    } else {
      null
    }
  }

  def propertyMissing(String name, value) {
    credentials.setProperty(name, credentialsEncryptor.encrypt(value as String))
  }

}
