@echo off

echo Running Maven build...
call mvn clean package
if %ERRORLEVEL% NEQ 0 (
    echo Maven build failed. Exiting.
    exit /b %ERRORLEVEL%
)

echo Starting event-service...
cd target
java -jar event-service-1.0-SNAPSHOT.jar

echo event-service started successfully
