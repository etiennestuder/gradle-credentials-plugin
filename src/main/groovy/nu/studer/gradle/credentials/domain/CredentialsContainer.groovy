package nu.studer.gradle.credentials.domain

import nu.studer.gradle.credentials.CredentialsPlugin

/**
 * Transiently retrieves and adds credentials.
 */
final class CredentialsContainer {

  private final Properties credentials

  CredentialsContainer(Properties initialCredentials) {
    // defensive copy
    this.credentials = new Properties();
    this.credentials.putAll(initialCredentials)
  }

  def propertyMissing(String name) {
    if (this.credentials.containsKey(name)) {
      Encryption encryption = Encryption.createEncryption(CredentialsPlugin.DEFAULT_PASSPHRASE.toCharArray())
      encryption.decrypt(this.credentials[name] as String)
    } else {
      null
    }
  }

  def propertyMissing(String name, value) {
    def encryption = Encryption.createEncryption(CredentialsPlugin.DEFAULT_PASSPHRASE.toCharArray())
    this.credentials[name] = value ? encryption.encrypt(value as String) : null
  }

}
