package nu.studer.gradle.credentials.domain

import nu.studer.java.util.OrderedProperties
import spock.lang.Specification

@SuppressWarnings("GrUnresolvedAccess")
class CredentialsContainerTest extends Specification {

    void "testGetUnknownPropertyReturnsNull"() {
        given:
        def encryptor = CredentialsEncryptor.withPassphrase("somePassphrase".toCharArray())
        def initialCredentials = new OrderedProperties()
        def container = new CredentialsContainer(encryptor, initialCredentials)

        when:
        def value = container.forKey('someKey')

        then:
        value == null
    }

}
