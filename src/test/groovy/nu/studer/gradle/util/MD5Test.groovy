package nu.studer.gradle.util

import spock.lang.Specification

class MD5Test extends Specification {

    void "generateMD5Hash"() {
        given:
        String input = "some string";

        when:
        String result = MD5.generateMD5Hash(input);

        then:
        for (char c : result.toCharArray()) {
            boolean isHex = '0' <= c && c <= '9' || 'A' <= c && c <= 'F';
            assert isHex
        }
    }

}
