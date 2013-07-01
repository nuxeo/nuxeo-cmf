#!/bin/bash
CURRENT_VERSION=1.8.2-HF08-SNAPSHOT
RELEASE_VERSION=1.8.2-HF08
WORKSPACE=release-$RELEASE_VERSION
#fix me
NX_REPO=/home/thierry/.m2/repository/org/nuxeo/cm
WORK_DIR=`pwd`
mkdir archives


fix_zip() {
    zip_path=$1
    zip_name=$2
    [  ! -z "$zip_name" ] && rm -rf /tmp/$zip_name
    unzip -q $zip_path -d /tmp/$zip_name
    cd /tmp/$zip_name
    [ ! -d "$zip_name" ] && mv nuxeo* $zip_name
    chmod +x $zip_name/bin/*ctl $zip_name/bin/*.sh $zip_name/bin/*.command $zip_name/*.command
    echo "nuxeo.wizard.done=false" >> $zip_name/bin/nuxeo.conf
    rm -f $zip_path
    zip -rq $zip_path $zip_name
    cd -
    rm -rf /tmp/$zip_name
}

prepare() {
    zip_source=$1
    zip_name=$2
    zip_path=$WORK_DIR/archives/$zip_name.zip
    [ -e $zip_path ] && rm -f $zip_path
    mv $zip_source $zip_path || exit 1
    fix_zip $zip_path $zip_name
    echo "### $zip_path done."
    (cd $WORK_DIR/archives; md5sum $zip_name.zip | tee $zip_name.zip.md5) || exit 1
}


# Change Application version version
find -name pom.xml |grep -v selenium|grep -v target|xargs sed -i s/$CURRENT_VERSION/$RELEASE_VERSION/g
sed -i s/$CURRENT_VERSION/$RELEASE_VERSION/g ./nuxeo-case-management-distribution/src/main/resources/cmf/nuxeo.defaults
sed -i s/$CURRENT_VERSION/$RELEASE_VERSION/g ./nuxeo-case-management-distribution/src/main/resources/correspondence/nuxeo.defaults
sed -i s/$CURRENT_VERSION/$RELEASE_VERSION/g ./nuxeo-case-management-distribution/src/main/resources/cmf-funkload/nuxeo.defaults


prepare() {
    zip_source=$1
    zip_name=$2
    zip_path=$WORK_DIR/archives/$zip_name.zip
    [ -e $zip_path ] && rm -f $zip_path
    mv $zip_source $zip_path || exit 1
    fix_zip $zip_path $zip_name
    echo "### $zip_path done."
    (cd $WORK_DIR/archives; md5sum $zip_name.zip | tee $zip_name.zip.md5) || exit 1
}


#Build and copy
mvn -Prelease,tomcat,server install

# Tomcat
prepare "$NX_REPO"/nuxeo-case-management-distribution/$RELEASE_VERSION/nuxeo-case-management-distribution-$RELEASE_VERSION-tomcat-cmf.zip nuxeo-case-management-distribution-$RELEASE_VERSION-tomcat-cmf

#deploy maven artifacts
mvn clean deploy  -Prelease,qa

cd $WORK_DIR/nuxeo-case-management-distribution/
mvn clean deploy  -Pserver,release,qa
mvn clean deploy  -Ptomcat,release,qa





