CORRESPONDENCE README
=====================

Correspondence is a packaged project on top of Nuxeo EP.  It adds
case management features to the applications, adding case and case items
document types that can be sent/received via case folders.

Install
-------

Requirements:

- Java 5
- Nuxeo > 5.2-M4 packaged with Jboss 4.2.3 GA

Instructions:

To deploy to an existing server:
- copy build.properties.sample to build.properties
- set the value of build.properties
- run "ant deploy"

To create a full distribution:
- run "mvn -Pserver clean install"
=>  the server is in the target directory
