gradle-credentials-plugin
=========================

# Overview

[Gradle](http://www.gradle.org) plugin that allows to store and access encrypted 
credentials using password-based encryption (PBE).

The credentials plugin is hosted at [Bintray's JCenter](https://bintray.com/etienne/gradle-plugins/gradle-credentials-plugin).

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
is used. The JDK encryption algorithm applied is _PBEWithMD5AndTripleDES_ using an 8-byte salt 
and an iteration count of 65536. Using a strong passphrase, this encryption is quite solid. 

Access to the stored credentials from within a Gradle build happens through the 
`credentials` project property. All read and write operations to the credentials container
apply the decryption and encryption on the fly. The credentials container never holds any 
credentials in their decrypted form.
 
Please note that the author of this plugin is by far not a security expert. It is also not
the primary goal of this plugin to provide high-security encryption, but rather to provide
a convenient way to avoid having to store credentials in plain text.

# Configuration

## Apply credentials plugin

TODO

# Feedback and Contributions

Both feedback and contributions are very welcome.

# Acknowledgements

None, yet.

# License

This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

(c) by Etienne Studer
