package nu.studer.gradle.credentials

import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading
import spock.lang.TempDir
import spock.lang.Unroll

@Unroll
class CredentialsFuncTest extends BaseFuncTest {

    @TempDir
    File tempFolder

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

    void "can configure custom location of password file"() {
        given:
        buildFile()
        def location = tempFolder

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

    void "tasks are registered lazily"() {
        given:
        buildFile()
        buildFile << """
tasks.withType(nu.studer.gradle.credentials.AddCredentialsTask).configureEach {
    println "configuring \$it"
}
tasks.withType(nu.studer.gradle.credentials.RemoveCredentialsTask).configureEach {
    println "configuring \$it"
}
"""

        when:
        def result = runWithArguments('help')

        then:
        result.task(':help').outcome == TaskOutcome.SUCCESS
        !result.output.contains('configuring')
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
"""
    }

}
