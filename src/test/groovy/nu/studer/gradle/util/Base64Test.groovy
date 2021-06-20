package nu.studer.gradle.util

import spock.lang.Specification

class Base64Test extends Specification {

    void "encodeDecode"() {
        given:
        byte[] textToEncode = "Some text that needs to be encoded.".getBytes("UTF-8");

        when:
        String encoded = Base64.encodeBase64(textToEncode);
        byte[] decoded = Base64.decodeBase64(encoded);

        then:
        decoded == textToEncode
    }

    void "printHexBinary"() {
        given:
        byte[] bytes = [-128, -1, 0, 9, 10, 11, 17, 127]

        when:
        String hex = Base64.printHexBinary(bytes);

        then:
        hex == "80FF00090A0B117F"
    }

}
