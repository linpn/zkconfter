#!/bin/bash
rm -rf com/
cd ../zkconfter
mvn -DaltDeploymentRepository=snapshot-repo::default::file:../mvn-repository clean deploy
