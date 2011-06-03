#!/bin/bash
CURRENT_VERSION=1.7-SNAPSHOT
RELEASE_VERSION=1.7
WORKSPACE=release-$RELEASE_VERSION


# Change Application version version
find -name pom.xml |grep -v selenium|grep -v target|xargs sed -i s/$CURRENT_VERSION/$RELEASE_VERSION/g
sed -i s/$CURRENT_VERSION/$RELEASE_VERSION/g ./nuxeo-case-management-distribution/src/main/resources/cmf/nuxeo.defaults
sed -i s/$CURRENT_VERSION/$RELEASE_VERSION/g ./nuxeo-case-management-distribution/src/main/resources/correspondence/nuxeo.defaults
sed -i s/$CURRENT_VERSION/$RELEASE_VERSION/g ./nuxeo-case-management-distribution/src/main/resources/cmf-funkload/nuxeo.defaults


#Build and copy
mvn -Ptomcat,tomcatCorr,server,serverCorr install


