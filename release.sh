#!/bin/bash
CURRENT_VERSION=1.9-SNAPSHOT
RELEASE_VERSION=1.9
WORKSPACE=release-$RELEASE_VERSION


# Change Application version version
find -name pom.xml |grep -v selenium|grep -v target|xargs sed -i s/$CURRENT_VERSION/$RELEASE_VERSION/g
sed -i s/$CURRENT_VERSION/$RELEASE_VERSION/g ./nuxeo-case-management-distribution/src/main/resources/cmf/nuxeo.defaults
sed -i s/$CURRENT_VERSION/$RELEASE_VERSION/g ./nuxeo-case-management-distribution/src/main/resources/correspondence/nuxeo.defaults
sed -i s/$CURRENT_VERSION/$RELEASE_VERSION/g ./nuxeo-case-management-distribution/src/main/resources/cmf-funkload/nuxeo.defaults


#Build and deploy the different packages
#mvn clean deploy -Pqa,release,tomcat,tomcatCorr,server,serverCorr 

#Package release
#mvn -PenableWizard,tomcat install
mkdir $WORKSPACE
cp -r ./nuxeo-case-management-distribution/target/stage/nuxeo-cm-tomcat $WORKSPACE/nuxeo-cmf-$RELEASE_VERSION-tomcat

cd $WORKSPACE/
chmod +x nuxeo-cmf-$RELEASE_VERSION-tomcat/*.command nuxeo-cmf-$RELEASE_VERSION-tomcat/bin/*ctl nuxeo-cmf-$RELEASE_VERSION-tomcat/bin/.sh nuxeo-cmf-$RELEASE_VERSION-tomcat/bin/*.command

zip -r nuxeo-cmf-$RELEASE_VERSION-tomcat.zip  nuxeo-cmf-$RELEASE_VERSION-tomcat
md5sum nuxeo-cmf-$RELEASE_VERSION-tomcat.zip | tee nuxeo-cmf-$RELEASE_VERSION-tomcat.md5 


