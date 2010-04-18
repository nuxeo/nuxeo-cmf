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
To create a distribution:
ant server
=> it will create a cm server in nuxeo-case-management-distribution/target
directory. 

To copy the jar file to the created server:
ant deploy-jar
