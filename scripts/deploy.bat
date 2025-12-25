@echo off
echo Compiling Scala project...
call sbt clean assembly

echo Copying JAR to Spark...
for %%f in (target\scala-2.12\*.jar) do (
    docker cp %%f spark-master:/opt/spark-apps/app.jar
)

echo Deployment completed!
pause