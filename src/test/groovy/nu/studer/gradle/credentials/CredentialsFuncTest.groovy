package nu.studer.gradle.credentials

import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Unroll

@Unroll
class CredentialsFuncTest extends BaseFuncTest {

  void setup() {
    new File(testKitDir, 'gradle.encrypted.properties').delete()
  }

  void "cannot access credentials added in same build execution"() {
    given:
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

    when:
    def result = runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', 'printValue', '-i')

    then:
    result.task(':addCredentials').outcome == TaskOutcome.SUCCESS
    result.output.contains('value: null')
  }

  void "can access credentials added in previous build execution"() {
    given:
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

    when:
    runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '-i')
    def result = runWithArguments('printValue', '-i')

    then:
    result.task(':printValue').outcome == TaskOutcome.SUCCESS
    result.output.contains('value: someValue')
  }

  void "can access credentials with dollar character in value"() {
    given:
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

    when:
    runWithArguments('addCredentials', '--key', 'someKey', '--value', 'before$after', '-i')
    def result = runWithArguments('printValue', '-i')

    then:
    result.task(':printValue').outcome == TaskOutcome.SUCCESS
    result.output.contains('value: before$after')
  }

  void "can access credentials added with custom passphrase"() {
    given:
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

    when:
    runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '-PcredentialsPassphrase=xyz', '-i')
    def result = runWithArguments('printValue', '-PcredentialsPassphrase=xyz', '-i')

    then:
    result.task(':printValue').outcome == TaskOutcome.SUCCESS
    result.output.contains('value: someValue')
  }

  void "cannot access credentials used with different passphrase from when added with custom passphrase"() {
    given:
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

    when:
    runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '-PcredentialsPassphrase=xyz', '-i')
    def result = runWithArguments('printValue', '-PcredentialsPassphrase=abz', '-i')

    then:
    result.task(':printValue').outcome == TaskOutcome.SUCCESS
    result.output.contains('value: null')
  }

  void "cannot access credentials used with different passphrase from when added with default passphrase"() {
    given:
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

    when:
    runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '-i')
    def result = runWithArguments('printValue', '-PcredentialsPassphrase=abz', '-i')

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

}
