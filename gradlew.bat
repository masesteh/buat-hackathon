@rem Gradle wrapper launcher for Windows
@echo off
set DIR=%~dp0
set CLASSPATH=%DIR%gradle\wrapper\gradle-wrapper.jar
java %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*