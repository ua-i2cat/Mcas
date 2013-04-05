#!/bin/bash

INSTALLATION_PATH=/home/i2cat/azure
LOGFILE="$INSTALLATION_PATH/logfile"
CLASSPATH="$INSTALLATION_PATH/lib/*:$INSTALLATION_PATH/bin/azure.jar"
MAINCLASS='cat.i2cat.mcaslite.service.TranscoService'

DIR=`pwd`
cd $INSTALLATION_PATH

exec 3>&1 4>&2 >$LOGFILE 2>&1
java -classpath $CLASSPATH $MAINCLASS &
exec 1>&3 2>&4

cd $DIR
