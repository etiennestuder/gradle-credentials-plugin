package nu.studer.gradle.credentials.domain
/**
 * Transiently retrieves and adds credentials.
 */
final class CredentialsContainer {

  private final CredentialsEncryptor credentialsEncryptor
  private final Properties credentials

  CredentialsContainer(CredentialsEncryptor credentialsEncryptor, Properties initialCredentials) {
    this.credentialsEncryptor = credentialsEncryptor
    this.credentials = new Properties();
    this.credentials.putAll(initialCredentials)
  }

  def propertyMissing(String name) {
    if (credentials.containsKey(name)) {
      credentialsEncryptor.decrypt(credentials[name] as String)
    } else {
      null
    }
  }

  def propertyMissing(String name, value) {
    this.credentials[name] = credentialsEncryptor.encrypt(value as String)
  }

}
