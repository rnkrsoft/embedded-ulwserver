#!/bin/sh
CURRENT_DIR=$(cd `dirname $0`; pwd)
if [ ! -z "$CLASSPATH" ] ; then
  CLASSPATH="$CLASSPATH":
fi
CLASSPATH="$CLASSPATH""$CURRENT_DIR"/boot.jar
MAINCLASS=org.springframework.boot.loader.JarLauncher
CMD_LINE_ARGS=
_RUNJAVA="java"
$_RUNJAVA -Dlog4j.configurationFile=log4j2.xml -classpath $CLASSPATH $MAINCLASS $CMD_LINE_ARGS