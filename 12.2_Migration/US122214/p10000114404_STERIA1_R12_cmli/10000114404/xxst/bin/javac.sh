#!/bin/sh -f
#
# $Header: javac.sh 0 2016-06-08 12:00:00 cemlisup noship $
#
# Revision: 1/31/2018 D. Sung, 
#           Check for file extension, allow compilation for java file only
#           3/15/2018 D. Sung, 
#           Suppress removal of file at the end of process
#
# Parameters:
#   first: destination path, i.e. $OA_JAVA/oracle
#   second: java file name
#   third: custom top java directory, i.e. xbol/java
#   fourth: (optional) skip compiler flag, i.e. nocomp
#
# convert custom top name (e.g. from xbol to XBOL_TOP)
utop=`echo $3 | awk -F"/" {'print $1'} | tr "[:lower:]" "[:upper:]"`"_TOP"

# extract java directory name
jdir=`echo $3 | awk -F"/" {'print $2'}`

# dereference custom top name to actual directory path
ctopd=`env | grep $utop | awk -F"=" {'print $2'}`"/${jdir}"

# create destination folder if necessary
if [ ! -d $1 ] ; then
  mkdir -p $1
fi

# copy file
cp ${ctopd}/$2 $1

# check if skip compiler flag is missing
if [ -z "$4" ];
then
  # javac location at 11i
  if [ -f $OA_JRE_TOP/bin/javac ] ;
  then
    $OA_JRE_TOP/bin/javac $1/$2
  # javac location at 12
  elif [ -f $OA_JRE_TOP/../bin/javac ] ;
  then
    $OA_JRE_TOP/../bin/javac $1/$2
  elif [ -f /usr/bin/javac ] ;
  then
    /usr/bin/javac $1/$2
  else
    javac $1/$2
  fi
fi
#rm ${ctopd}/$2
