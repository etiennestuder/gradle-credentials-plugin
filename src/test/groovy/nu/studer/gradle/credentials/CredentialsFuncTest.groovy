package nu.studer.gradle.credentials

import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Unroll

@Unroll
class CredentialsFuncTest extends BaseFuncTest {

    @Rule
    TemporaryFolder tempFolder

    void setup() {
        new File(testKitDir, 'gradle.encrypted.properties').delete()
    }

    void "cannot add credentials with null key"() {
        given:
        buildFile()

        when:
        def result = runAndFailWithArguments('addCredentials', '--value', 'someValue', '-i')

        then:
        result.task(':addCredentials').outcome == TaskOutcome.FAILED
        result.output.contains('Credentials key must not be null')
    }

    void "cannot add credentials with null value"() {
        given:
        buildFile()

        when:
        def result = runAndFailWithArguments('addCredentials', '--key', 'someKey', '-i')

        then:
        result.task(':addCredentials').outcome == TaskOutcome.FAILED
        result.output.contains('Credentials value must not be null')
    }

    void "cannot access credentials added in same build execution"() {
        given:
        buildFile()

        when:
        def result = runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', 'printValue', '-i')

        then:
        result.task(':addCredentials').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: null')
    }

    void "can access credentials added in previous build execution"() {
        given:
        buildFile()

        when:
        runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '-i')
        def result = runWithArguments('printValue', '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: someValue')
    }

    void "can access credentials with dollar character in value"() {
        given:
        buildFile()

        when:
        runWithArguments('addCredentials', '--key', 'someKey', '--value', 'before$after', '-i')
        def result = runWithArguments('printValue', '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: before$after')
    }

    void "can access credentials added with custom passphrase"() {
        given:
        buildFile()

        when:
        runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '-PcredentialsPassphrase=xyz', '-i')
        def result = runWithArguments('printValue', '-PcredentialsPassphrase=xyz', '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: someValue')
    }

    void "can access credentials added with custom passphrase and some custom env"() {
        given:
        buildFile()

        when:
        runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '-PcredentialsPassphrase=xyz', '-PcredentialsEnv=abc', '-i')
        def result = runWithArguments('printValue', '-PcredentialsPassphrase=xyz', '-PcredentialsEnv=abc', '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: someValue')
    }

    void "cannot access credentials used with different passphrase from when added with custom passphrase"() {
        given:
        buildFile()

        when:
        runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '-PcredentialsPassphrase=xyz', '-i')
        def result = runWithArguments('printValue', '-PcredentialsPassphrase=abz', '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: null')
    }

    void "cannot access credentials used with different passphrase from when added with default passphrase"() {
        given:
        buildFile()

        when:
        runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '-i')
        def result = runWithArguments('printValue', '-PcredentialsPassphrase=abz', '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: null')
    }

    void "can access credentials by key or key-hash"() {
        given:
        buildFile()

        when:
        runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '-i')
        def result = runWithArguments('printValue', '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: someValue')

        when:
        result = runWithArguments('printValueHash', '-i')

        then:
        result.task(':printValueHash').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: someValue')
    }

    void "cannot access credentials used with different env from when added with default env"() {
        given:
        buildFile()

        when:
        runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '-i')
        def result = runWithArguments('printValue', '-PcredentialsEnv=abz', '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: null')
    }

    void "can configure custom --loc of password file"() {
        given:
        buildFile()
        def location = tempFolder.newFolder()

        when:
        runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '--loc', location.canonicalPath, '-i')
        def result = runWithArguments('printValue', '-PcredentialsLocation=' + location.canonicalPath, '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: someValue')

        when:
        result = runWithArguments('printValue', '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: null')
    }

    void "can configure custom --loc of password file and custom --env"() {
        given:
        buildFile()
        def location = tempFolder.newFolder()

        when:
        runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '--loc', location.canonicalPath, '--env', 'someEnv' , '-i')
        println "PATH: ${location.canonicalPath}"
        def result = runWithArguments('printValue', '-PcredentialsLocation=' + location.canonicalPath, '-PcredentialsEnv=someEnv', '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        println "TASK0: ${result.output}"
        result.output.contains('value: someValue')

        when:
        result = runWithArguments('printValue', '-PcredentialsLocation=' + location.canonicalPath, '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        println "TASK1: ${result.output}"
        result.output.contains('value: null')
    }

    void "custom --loc overrides -PcredentialsLocation= of password file"() {
        given:
        buildFile()
        def location = tempFolder.newFolder()
        def testLocation = tempFolder.newFolder()

        when:
        runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '--loc', location.canonicalPath, '-PcredentialsLocation=' + testLocation.canonicalPath, '-i')
        def result = runWithArguments('printValue', '-PcredentialsLocation=' + location.canonicalPath, '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: someValue')

        when:
        result = runWithArguments('printValue', '-PcredentialsLocation=' + testLocation.canonicalPath, '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: null')
    }

    void "can configure custom location of password file"() {
        given:
        buildFile()
        def location = tempFolder.newFolder()

        when:
        runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '-PcredentialsLocation=' + location.canonicalPath, '-i')
        def result = runWithArguments('printValue', '-PcredentialsLocation=' + location.canonicalPath, '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: someValue')

        when:
        result = runWithArguments('printValue', '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: null')

    }

    void "can apply plugin in conjunction with the maven publish plugins"() {
        given:
        buildFile << """
plugins {
    id 'nu.studer.credentials'
    id 'maven-publish'
}

publishing {
    publications {
        something(MavenPublication) { artifact file('build.gradle') }
    }
}

task printValue {
  doLast {
    String val = credentials.someKey
    println "value: \$val"
  }
}
"""

        when:
        runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '-i')
        def result = runWithArguments('printValue', '-i')

        then:
        result.task(':printValue').outcome == TaskOutcome.SUCCESS
        result.output.contains('value: someValue')
    }

    void "can apply plugin and access credentials in settings.gradle"() {
        given:
        buildFile << """
plugins {
    id 'nu.studer.credentials'
}
"""

        and:
        runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '-i')

        and:
        settingsFile << """
buildscript {
    dependencies {
        classpath files(${implClasspath()})
    }
}

apply plugin: 'nu.studer.credentials'

String val = credentials.someKey
println "value: \$val"
"""

        when:
        def result = runWithArguments()

        then:
        result.output.contains('value: someValue')
    }

    private static def implClasspath() {
        PluginUnderTestMetadataReading.readImplementationClasspath().collect { it.absolutePath.replace('\\', '\\\\') }.collect { "'$it'" }.join(",")
    }

    private File buildFile() {
        buildFile << """
plugins {
    id 'nu.studer.credentials'
}

task printValue {
  doLast {
    String val = credentials.someKey
    println "value: \$val"
  }
}

task printValueHash {
  doLast {
    String val = credentials['3C3C2D011C2953B001B12407E135ABA5']
    println "value: \$val"
  }
}
"""
    }

}
