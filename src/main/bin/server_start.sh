#!/bin/sh
# SERVER_CLASS=$1               # PROGRAM_HOME=$2               # PID_FILE=$PROGRAM_HOME/logs/$3        ?¡¤# CONSOLE_LOG_FILE=$PROGRAM_HOME/logs/$4       ?¡¤# PROGRAM_HOME_NAME=$5
# MEM_OPTIONS=$6
# ? $PROGRAM_HOME/lib/classes 


if [ $# -lt 6 ]
then
        echo "at least 6 params required!"
        echo "        param1: server class name"
        echo "        param2: home path"
        echo "        param3: pid file name"
        echo "        param4: log file name"
        echo "        param5: program home name"
        echo "        param6: jvm memory options ( optional )"
        exit 1
fi


SERVER_CLASS=$1
PROGRAM_HOME=$2
PID_FILE=$PROGRAM_HOME/logs/$3
LOG_FILE=$PROGRAM_HOME/logs/$4
PROGRAM_HOME_NAME=$5

# ?m

     if [ -z "$7" ]
then
        MEM_OPTIONS="-Xms2g -Xmx2g -XX:NewRatio=4 -XX:SurvivorRatio=8"
else
        MEM_OPTIONS=$7
fi

echo "SERVER_CLASS: $SERVER_CLASS"
echo "PROGRAM_HOME: $PROGRAM_HOME"
echo "PID_FILE: $PID_FILE"
echo "LOG_FILE: $LOG_FILE"
echo "PROGRAM_HOME_NAME:$PROGRAM_HOME_NAME"
echo "MEM_OPTIONS: $MEM_OPTIONS"

CP=.:$PROGRAM_HOME/lib/classes:$PROGRAM_HOME/conf
export CP
for file in `find $PROGRAM_HOME/lib -name '*jar' | sort` ;
do
        CP=$CP:$file;
done;

echo "CP: $CP"
export CP

if [ -f $PID_FILE ]; then \
#        kill `cat $PID_FILE` 2>/dev/null;
        echo "running server $SERVER_CLASS (pid: `cat $PID_FILE`) killed"
fi;
mv $LOG_FILE $LOG_FILE.`date +%y-%m-%d-%H-%M` 2> /dev/null
java -server -Djava.net.preferIPv4Stack=true -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=9992 -Dsun.lang.ClassLoader.allowArraySyntax=true $MEM_OPTIONS -XX:TargetSurvivorRatio=90 -XX:MaxTenuringThreshold=4 -XX:+PrintTenuringDistribution -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -verbose:gc -cp $CP -D$PROGRAM_HOME_NAME=$PROGRAM_HOME $SERVER_CLASS server $PROGRAM_HOME >$LOG_FILE 2>&1 &
echo $! > $PID_FILE
echo "server $SERVER_CLASS (pid: `cat $PID_FILE`) started"
exit 0;
