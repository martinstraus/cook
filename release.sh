#!/bin/bash

function usage() {
    echo "Usage"
    echo "   ./release.sh VV.vv.rr"
}

version=$1
message=$2

if [ -z "${version}" ]
then
    usage
    exit 1
fi

mvn versions:set -DnewVersion=${version}

mvn clean install

if [ "$?" != "0" ]
then 
    mvn versions:revert
    exit 2
fi

mvn versions:commit
git add pom.xml
git commit -m "Release version ${version}."
git push 

git tag -a v${version} -m "${message}"
git push --tags origin
