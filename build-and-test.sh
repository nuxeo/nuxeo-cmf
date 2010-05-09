#!/bin/bash -x

# Build Nuxeo Case Management
mvn clean install || exit 1
mvn -Pserver -f nuxeo-case-management-distribution/pom.xml clean install || exit 1

# start JBoss
(cd nuxeo-case-management-distribution/target && unzip nuxeo-case-management-distribution-*.zip && rm *.zip) || exit 1
chmod +x nuxeo-case-management-distribution/target/nuxeo-cm-server/bin/nuxeoctl || exit 1
nuxeo-case-management-distribution/target/nuxeo-cm-server/bin/nuxeoctl start || exit 1

# Run selenium tests
HIDE_FF=true nuxeo-case-management-distribution/ftest/selenium/run.sh
ret1=$?

# Strop JBoss
nuxeo-case-management-distribution/target/nuxeo-cm-server/bin/nuxeoctl stop || exit 1

# Exit if some tests failed
[ $ret1 -eq 0 ] || exit 9
