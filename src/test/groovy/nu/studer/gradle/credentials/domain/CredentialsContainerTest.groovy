package nu.studer.gradle.credentials.domain

import nu.studer.java.util.OrderedProperties

@SuppressWarnings("GrUnresolvedAccess")
class CredentialsContainerTest extends GroovyTestCase {

  void testSetGetCredentials() {
    def encryptor = CredentialsEncryptor.withPassphrase("somePassphrase".toCharArray());
    def initialCredentials = new OrderedProperties()
    def container = new CredentialsContainer(encryptor, initialCredentials)
    def value = 'someValue'

    container.someKey = value
    def actualValue = container.someKey

    assertEquals(value, actualValue)
  }

  void testGetUnknownPropertyReturnsNull() {
    def encryptor = CredentialsEncryptor.withPassphrase("somePassphrase".toCharArray());
    def initialCredentials = new OrderedProperties()
    def container = new CredentialsContainer(encryptor, initialCredentials)

    def value = container.someKey

    assertNull(value)
  }

}
