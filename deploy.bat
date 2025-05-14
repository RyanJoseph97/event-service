@echo off
set WEBAPPS_PATH=C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\

echo Running Maven build...
call mvn clean install
if %ERRORLEVEL% NEQ 0 (
    echo Maven build failed. Exiting.
    exit /b %ERRORLEVEL%
)

echo Moving files to webapps directory...

if exist target\event-service.war (
    move /Y target\event-service.war "%WEBAPPS_PATH%"
) else (
    echo event-service.war not found!
)

if exist target\event-service (
    xcopy /E /I /Y "target\event-service" "%WEBAPPS_PATH%\event-service"
) else (
    echo event-service directory not found!
)

echo Done moving event-service files

call catalina.bat jpda start