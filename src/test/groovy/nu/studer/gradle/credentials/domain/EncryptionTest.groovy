package nu.studer.gradle.credentials.domain

import spock.lang.Specification

class EncryptionTest extends Specification {

    void "encryptDecryptAcrossDifferentEncryptionInstances"() {
        given:
        char[] passphrase = "My very secret pass phrase".toCharArray()
        String textToEncrypt = "Some text that needs to be encrypted."

        when:
        Encryption encryption = Encryption.createEncryption(passphrase)
        String encrypted = encryption.encrypt(textToEncrypt)

        Encryption encryption2 = Encryption.createEncryption(passphrase)
        String decrypted = encryption2.decrypt(encrypted)

        then:
        textToEncrypt == decrypted
    }

}
