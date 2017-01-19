del com/
cd ../
call mvn -DaltDeploymentRepository=snapshot-repo::default::file:mvn-repository clean deploy
@pause
