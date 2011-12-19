CASE MANAGEMENT FRAMEWORK
=========================

Nuxeo Case Management Framework is a framework based on our open source ECM platform Nuxeo Enterprise Platform (EP). It enables you to create applications dedicated to management of documents composed of items that evolve by being transfered to different persons responsible for their review or approval. A case can be for instance: a loan case composed of the different documents required for loan processing; a mail envelope with one or several documents; a legal case, etc.

User guide:
http://doc.nuxeo.com/x/JwAz

Developer guide:
http://doc.nuxeo.com/x/TQA7


Install
-------

Requirements:

- Java 6
- Maven 2.2.1


Instructions:
To create a CMF tomcat distribution:

    mvn clean install -Ptomcat

Server is then available in nuxeo-case-management-distribution/target directory. 


    mvn clean install -Pserver

Creates a CMF jboss distribution server.

    mvn clean install -PtomcatCorr

Creates a Correspondance tomcat distribution server.

    mvn clean install -PserverCorr

Creates a Correspondance Jboss distribution server.


