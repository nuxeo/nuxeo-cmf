#!/bin/bash -x
PROFILE=${1:-"server"}
DISTRIBUTION=${2:-$PROFILE}
DIR_DISTRIB=${3:-"cm"}

echo "using distribution: $DISTRIBUTION"
# Build Nuxeo Case Management
mvn -Dmaven.test.skip=true clean install || exit 1
mvn -P$PROFILE -f nuxeo-case-management-distribution/pom.xml clean install || exit 1

# start JBoss
(cd nuxeo-case-management-distribution/target && unzip nuxeo-case-management-distribution-*.zip && rm *.zip) || exit 1
chmod +x nuxeo-case-management-distribution/target/nuxeo-${DIR_DISTRIB}-$DISTRIBUTION/bin/nuxeoctl || exit 1
sed -i "s/-Xmx1024m/-Xmx2g/" nuxeo-case-management-distribution/target/nuxeo-${DIR_DISTRIB}-$DISTRIBUTION/bin/nuxeo.conf

nuxeo-case-management-distribution/target/nuxeo-${DIR_DISTRIB}-$DISTRIBUTION/bin/nuxeoctl start || exit 1

# Run selenium tests
HIDE_FF=true nuxeo-case-management-distribution/ftest/selenium/run.sh
ret1=$?

# Stop JBoss
nuxeo-case-management-distribution/target/nuxeo-${DIR_DISTRIB}-$DISTRIBUTION/bin/nuxeoctl stop || exit 1

# Exit if some tests failed
[ $ret1 -eq 0 ] || exit 9
