@echo off
set "CURRENT_DIR=%cd%"
if "%CLASSPATH%" == "" goto emptyClasspath
set "CLASSPATH=%CLASSPATH%;"
:emptyClasspath
set "CLASSPATH=%CLASSPATH%%CURRENT_DIR%\boot.jar"
set MAINCLASS=org.springframework.boot.loader.JarLauncher
set _RUNJAVA="%JAVA_HOME%\bin\java.exe"
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs

:doneSetArgs
%_RUNJAVA% -Dlog4j.configurationFile=log4j2.xml -classpath %CLASSPATH% %MAINCLASS% %CMD_LINE_ARGS%
goto end
:end