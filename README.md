<p align="left">
  <a href="https://github.com/etiennestuder/gradle-credentials-plugin/actions?query=workflow%3A%22Build+Gradle+project%22"><img src="https://github.com/etiennestuder/gradle-credentials-plugin/workflows/Build%20Gradle%20project/badge.svg"></a>
</p>

gradle-credentials-plugin
=========================

> The work on this software project is in no way associated with my employer nor with the role I'm having at my employer. Any requests for changes will be decided upon exclusively by myself based on my personal preferences. I maintain this project as much or as little as my spare time permits.

# Overview

[Gradle](http://www.gradle.org) plugin that allows to store and access encrypted
credentials using password-based encryption (PBE).

The credentials plugin is hosted at the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/nu.studer.credentials).

## Build scan

Recent build scan: https://scans.gradle.com/s/vzlk2e4dnfzge

Find out more about build scans for Gradle and Maven at https://scans.gradle.com.

# Goals

One typical use case of the 'gradle.properties' file in the Gradle user home directory is
to store credentials, and to reference them from Gradle builds as project properties. This
is a very convenient functionality at the cost that, by default, these properties are stored
in plain text. It happens quickly that such credentials are exposed accidentally while giving
a Gradle presentation or while pair-programming with a colleague.

The credentials plugin provides a parallel functionality to the 'gradle.properties' file to
store and access credentials in an encrypted format through a 'gradle.encrypted.properties'
files, thereby avoiding that credentials are ever stored in plain text.

# Functionality

The following functionality is provided by the credentials plugin:

 * Store encrypted credentials
 * Delete encrypted credentials
 * Access encrypted credentials from a Gradle build

# Design

All access and storage of credentials goes through password-based encryption. The passphrase
can either be specified as a project property from the command line, or a default passphrase
is used. The JDK encryption algorithm applied is _AES_ using a key that is generated using
_PBKDF2WithHmacSHA1_ from an 8-byte salt, an iteration count of 65536, and a key length of
128 (longer keys require local installation of the JRE Security Extension).

Access to the stored credentials from within a Gradle build happens through the
`credentials` project property. All read and write operations to the credentials container
apply the decryption and encryption on the fly. The credentials container never holds any
credentials in their decrypted form.

Please note that the author of this plugin is by far not a security expert. It is also not
the primary goal of this plugin to provide high-security encryption, but rather to provide
a convenient way to avoid having to store credentials in plain text.

# Configuration

## Apply credentials plugin

### Project-application

Apply the `nu.studer.credentials` plugin to your Gradle project.

```groovy
plugins {
    id 'nu.studer.credentials' version '3.0'
}
```

### Settings-application

Apply the `nu.studer.credentials` plugin to your Gradle settings file.

```groovy
plugins {
    id 'nu.studer.credentials' version '3.0'
}
```

## Invoke credentials tasks

### Store encrypted credentials

You can store new credentials or update existing credentials through the `addCredentials` task. Pass along
the credentials key and value through the task options `--key` and `--value`. The
credentials are stored in the _GRADLE_USER_HOME/gradle.encrypted.properties_.

    gradle addCredentials --key someKey --value someValue

Optionally, pass along a custom passphrase through the `credentialsPassphrase` project property.
Passing a passphrase is recommended and might be required in future releases of the credentials plugin.

    gradle addCredentials --key someKey --value someValue -PcredentialsPassphrase=mySecretPassPhrase

Optionally, pass along a custom directory location of the credentials file through the `credentialsLocation` project property.

    gradle addCredentials --key someKey --value someValue -PcredentialsLocation=/some/directory

Optionally, specify the file name of the credentials file through the `credentialsFile` project property.

    gradle addCredentials --key someKey --value someValue -PcredentialsFile=my.encrypted.properties

Specifying a custom `credentialsFile` project property can be useful to encrypt credentials for different projects with
different passphrases.

### Remove encrypted credentials

You can remove existing credentials through the `removeCredentials` task. Pass along
the credentials key through the `--key` project property. The credentials are removed from the
_GRADLE_USER_HOME/gradle.encrypted.properties_.

    gradle removeCredentials --key someKey

Optionally, pass along a custom directory location of the credentials file through the `credentialsLocation` project property.

    gradle removeCredentials --key someKey -PcredentialsLocation=/some/directory

Optionally, specify the file name of the credentials file through the `credentialsFile` project property.

    gradle addCredentials --key someKey --value someValue -PcredentialsFile=my.encrypted.properties

## Access credentials in build

### Get credentials from within a build

Get the desired credentials from the `credentials` container, available on the project instance. The
credentials are decrypted as they are accessed.

```groovy
String accountPassword = credentials.forKey('someAccountPassword')
```

Per default, the `credentials` container is initialized with all credentials persisted in the _GRADLE_USER_HOME/gradle.encrypted.properties_.

If a custom directory location is passed through the `credentialsLocation` project property when starting the build, the credentials file will be seeked in that directory.

A custom credentials file name can be specified via the `credentialsFile` project property.

# Compatibility

|Plugin version|Compatible Gradle versions|Support for Gradle Kotlin DSL|Support for Gradle Configuration Cache| Minimum JDK |
|--------------|---------------------------|----------------------------|--------------------------------------|-------------|
| 3.0+         | 6.0+, 7.0+                | Yes                        | N/A                                  | 8           |
| 2.0+         | 6.0+, 7.0+                | No                         | N/A                                  | 8           |

See the [Migration](#migration) section on how to migrate your build from older to newer credentials plugin versions.

# Migration

## Migrating from credentials plugin 3.x to 4.x

When specifying a passphrase the file name of the encrypted properties no longer contains the MD5 digest of the passphrase.
Previously, the file name was `gradle.MD5HASH.encrypted.properties`. Since version 4.0 the default file name `gradle.encrypted.properties`
is used, unless a custom file name is specified via the `credentialsFile` project property.

Please rename the file from the name containing the MD5 digest to the default filename `gradle.encrypted.properties` or specify the
file name via the `credentialsFile` project property.

Removing the MD5 digest from the file name and updating the passphrase and credentials is recommend.

## Migrating from credentials plugin 2.x to 3.x

When migrating your build from credentials plugin 2.x to 3.x, follow these steps:

- Access the keys via new `credentials.forKey` API. For example, `credentials.forKey('someAccountPassword')` instead of `credentials.someAccountPassword`.
- Remove all usages of setting credentials in your build at runtime. For example, `credentials.someAccountPassword = 'chocolate'`. Instead, use the `addCredentials` Gradle task to
  make credentials available to the build.

# Examples

## Project-application

You can find a self-contained example build script [here](example/project_application/build.gradle).

## Settings-application

The credentials plugin can also be applied to a Gradle settings file. You can find a self-contained example build script [here](example/settings_application/settings.gradle).

# Feedback and Contributions

Both feedback and contributions are very welcome.

# Acknowledgements

+ [Myllyenko](https://github.com/Myllyenko) (pr)
+ [aingram](https://github.com/aingram) (pr)

# License

This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

(c) by Etienne Studer
