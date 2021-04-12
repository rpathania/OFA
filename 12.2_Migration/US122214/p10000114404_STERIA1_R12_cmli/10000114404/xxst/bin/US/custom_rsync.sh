#!/bin/bash

# $Header: custom_rsync.sh 0 2016-06-14 12:00:00 cemlisup $
# This script will iterate through the top_list variable containg one custom top and java top,
# generate the appropriate rsync command to replicate the current base to the remote base for each top.
# Prior to calling this script:
# 1. Replace the #< TOP_LIST_VAR > with the top_list declaration such as:
#    top_list="XBOL_TOP JAVA_TOP"
# 2. Replace the #< JAVATOP_VAR > with the custom top only
#    javatop="xbol"
#    (6/10/2016, change to rsync entire java top, so this parameter is not used, but keep for future need)

# Declare a variable to hold the one custom top and java top to process:
top_list="XXST_TOP JAVA_TOP"
#echo "top_list=$top_list"

# Declare a variable to hold the one custom top to process:
javatop="xxst"
#echo "javatop=$javatop"

# Retrieve current instance path
current_base=`grep s_current_base $CONTEXT_FILE | awk -F">" {'print $2'} | awk -F"<" {'print $1'}`
#echo "current_base=$current_base"

# Retrieve remote instance path
other_base=`grep s_other_base $CONTEXT_FILE | awk -F">" {'print $2'} | awk -F"<" {'print $1'}`
#echo "other_base=$other_base"
#echo ""

# Iterate through all custom tops
for one_top in $top_list;
do
  #echo "one_top=$one_top"

  # grep custom top entry from context file
  ctop=`grep -i -e "<"$one_top $CONTEXT_FILE`
  #echo "Custom Top=$ctop"

  # parse custome top directory
  ctop_dir=`echo $ctop | awk -F">" {'print $2'} | awk -F"<" {'print $1'}`
  #echo "Custom Top Directory=$ctop_dir"

  # if custom top directory begins with the current instance path
  if [[ "$ctop_dir" == "$current_base"* ]];
  then
    #echo "Good Custom Top";

    # strip current instance path from custom top directory
    ctop_path=`echo ${ctop_dir#$current_base}`
    #echo "Custom Top Path=$ctop_path"

    # D. Sung, 6/14/2016, no need to strip last subdirectory for any top
    # if not java top then strip the last subdirectory
    # if [[ "$one_top" != "JAVA_TOP" ]];
    # then
    #   otop_path=$(dirname "${ctop_path}")
    # else
    #   #ctop_dir=$ctop_dir"/"$javatop"/oracle"
    #   #otop_path=$ctop_path"/"$javatop
    #   otop_path=$ctop_path
    # fi
    otop_path=$ctop_path
    #echo "otop_path=$otop_path"

    # rsync command
    # D. Sung, 6/14/2016, add trailing "/" to source and target paths
    ctop_dir=${ctop_dir}"/"
    otop_path=${otop_path}"/"
    #echo "Rsync for $one_top:"
    #echo "/usr/bin/rsync -azr $ctop_dir $other_base$otop_path"
    if [ -d $ctop_dir ] && [ -d $other_base$otop_path ];
    then
      #echo "$ctop_dir and $other_base$otop_path exists"
      /usr/bin/rsync -azr $ctop_dir $other_base$otop_path
    fi

  else
    :
    #echo "Bad Custom Top $one_top";
  fi
  #echo ""
done
