package nu.studer.gradle.credentials.domain

@SuppressWarnings("GrUnresolvedAccess")
class CredentialsContainerTest extends GroovyTestCase {

  void testSetGetCredentials() {
    def encryptor = CredentialsEncryptor.withPassphrase("somePassphrase".toCharArray());
    def initialCredentials = []
    def container = new CredentialsContainer(encryptor, initialCredentials as Properties)
    def value = 'someValue'

    container.someKey = value
    def actualValue = container.someKey

    assertEquals(value, actualValue)
  }

  void testGetUnknownPropertyReturnsNull() {
    def encryptor = CredentialsEncryptor.withPassphrase("somePassphrase".toCharArray());
    def initialCredentials = []
    def container = new CredentialsContainer(encryptor, initialCredentials as Properties)

    def value = container.someKey

    assertNull(value)
  }

}
