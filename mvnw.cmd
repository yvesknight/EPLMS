@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET @@MVNW_LAUNCHER=%~dp0.mvn\wrapper\maven-wrapper.jar

@SET MAVEN_PROJECTBASEDIR=%~dp0
@IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" SET MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@SET DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar

@SET JAVA_HOME_CANDIDATE=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
@IF EXIST "%JAVA_HOME_CANDIDATE%\bin\java.exe" SET JAVA_HOME=%JAVA_HOME_CANDIDATE%

@SET JAVA_EXE=java.exe
@IF NOT "%JAVA_HOME%"=="" SET JAVA_EXE=%JAVA_HOME%\bin\java.exe

@SET WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar

@IF NOT EXIST "%WRAPPER_JAR%" (
    @echo Downloading Maven Wrapper...
    @powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%WRAPPER_JAR%'}"
)

@"%JAVA_EXE%" ^
  -classpath "%WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %WRAPPER_LAUNCHER% %*
