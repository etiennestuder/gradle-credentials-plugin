package nu.studer.gradle.credentials

import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Unroll

@Unroll
class CredentialsFuncTest extends BaseFuncTest {

  void setup() {
    def gradleBuildCacheDir = new File(testKitDir, "caches/build-cache-1")
    gradleBuildCacheDir.deleteDir()
    gradleBuildCacheDir.mkdir()
  }

  void "can apply plugin"() {
    given:
    buildFile << """
plugins {
    id 'nu.studer.credentials'
}
"""

    when:
    def result = runWithArguments('addCredentials', '--key', 'someKey', '--value', 'someValue', '-i')

    then:
    result.task(':addCredentials').outcome == TaskOutcome.SUCCESS
  }

}
