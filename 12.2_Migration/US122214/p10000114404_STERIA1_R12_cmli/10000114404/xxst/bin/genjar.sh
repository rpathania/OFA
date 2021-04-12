#!/bin/bash

# $Header: genjar.sh 0 2020-02-24 12:00:00 cemlisup $
# This script will check if the environment is 12.2, if so it will call the adcgnjar script to generate the jar files
# Parameters:
#   first: apps username
#   second: apps password
#   third: not needed

echo "Start the generate jar files script"

if [ "$(printf '%s\n' "12.2.0" "$APPS_VERSION" | sort -V | head -n1)" = "12.2.0" ] ; then
       echo "Environment is $APPS_VERSION"
       if [ ! -d $OA_JAVA/../custom ] ; then
         echo "Create the $OA_JAVA/../custom directory"
         mkdir -p $OA_JAVA/../custom
       fi
       echo "Run adcgnjar to generate customall.jar file"
       { sleep 1; echo $1; sleep 1; echo $2;} | adcgnjar > $OA_JAVA/genjar.log
       for (( c=1; c<=5; c++ ))
        do
           if [  "`grep -c "ERROR" $OA_JAVA/genjar.log`" -gt 0 ] ; then
               echo "Error during generate jar files - Retrying"
               sleep 10s
               { sleep 1; echo $1; sleep 1; echo $2;} | adcgnjar > $OA_JAVA/genjar.log
           else
               break
           fi
       done
fi
