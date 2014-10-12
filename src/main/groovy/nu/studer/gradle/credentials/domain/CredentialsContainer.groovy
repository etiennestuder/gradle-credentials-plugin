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
    this.credentials = initialCredentials
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
