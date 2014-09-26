package nu.studer.gradle.credentials.domain

@SuppressWarnings("GrUnresolvedAccess")
class CredentialsContainerTest extends GroovyTestCase {

  void testSetGetCredentials() {
    def initialCredentials = []
    def container = new CredentialsContainer(initialCredentials as Properties)
    def value = 'someValue'

    container.someKey = value
    def actualValue = container.someKey

    assertEquals(value, actualValue)
  }

  void testGetUnknownPropertyReturnsNull() {
    def container = new CredentialsContainer(new Properties())

    def value = container.someKey

    assertNull(value)
  }

}
