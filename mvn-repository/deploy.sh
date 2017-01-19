#!/bin/bash
rm -rf com/
cd ../
mvn -DaltDeploymentRepository=snapshot-repo::default::file:mvn-repository clean deploy
