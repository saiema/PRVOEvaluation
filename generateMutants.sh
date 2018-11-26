#!/bin/bash

#set -x

#Documentation                                                          #
#Runs muJava only for mutant generation
#
#Arguments
propertiesTemplateFile="$1"
#
#Constants
CURRENT_DIR=$(pwd)
#JUNIT, EVOSUITE, AND RANDOOP
JUNIT="${CURRENT_DIR}/tools/junit-4.12.jar"
HAMCREST="${CURRENT_DIR}/tools/org.hamcrest.core_1.3.0.v201303031735.jar"
#MUJAVA
MUJAVA_HOME="tools/mujava"
MUJAVA_LIB_DIR=$MUJAVA_HOME"/libs"
MUJAVA_JAR=$MUJAVA_HOME"/mujava++.jar"
pushd $MUJAVA_LIB_DIR
MUJAVA_LIBS=$(find . | awk '/\.jar/' | sed "s/\\.\\///g" | xargs -I {} echo $MUJAVA_LIB_DIR/{}":")
MUJAVA_LIBS="${MUJAVA_LIBS::-1}"
MUJAVA_LIBS=$(tr -d "\n\r" < <(echo $MUJAVA_LIBS))
MUJAVA_LIBS=$(echo $MUJAVA_LIBS | sed "s/ //g")
popd
#
#########################################################################


pushd () {
    command pushd "$@" > /dev/null
}

popd () {
    command popd "$@" > /dev/null
}

#Runs muJava++ with a specific properties file
#classpath	: the classpath to use
#propertiesFile	: the properties file to use
function mujava() {
	echo "Running MuJava++..."
	local classpath="$1"
	local propertiesFile="$2"
	java -cp "$classpath:$MUJAVA_LIBS:$MUJAVA_JAR" mujava.app.Main --properties $propertiesFile 1>${propertiesFile}.out 2>${propertiesFile}.err
}


#MAIN
[ ! -e $propertiesTemplateFile ] && exit 101
[[ ! $propertiesTemplateFile == *\.properties ]] && exit 102
[[ ! $propertiesTemplateFile == *\_template.properties ]] && exit 103
binDir=$(grep -o "path.original.bin= .*$" $propertiesTemplateFile)
sourceDir=$(grep -o "path.original.source= .*$" $propertiesTemplateFile)
mutantDir=$(grep -o "path.mutants= .*$" $propertiesTemplateFile)
[ -z "$binDir" ] && exit 105
[ -z "$sourceDir" ] && exit 107
[ -z "$mutantDir" ] && exit 109
binDir=$(echo "$binDir" | sed 's/path\.original\.bin= //g')
sourceDir=$(echo "$sourceDir" | sed 's/path\.original\.source= //g')
mutantDir=$(echo "$mutantDir" | sed 's/path\.mutants= //g')
[ -e "${mutantDir}" ] && [ -n "$(ls -A ${mutantDir})" ] && exit 111
propertiesFile="$propertiesTemplateFile"

#TIMES
mujavaTime=""
############################################################################################################
#MUTATION
START=$(date +%s.%N)
mujava "${CURRENT_DIR}/${binDir}:${CURRENT_DIR}/${testDir}" $propertiesFile
ecode="$?"
[[ "$ecode" -ne "0" ]] && exit 601
END=$(date +%s.%N)
mujavaTime=$(echo "$END - $START" | bc)
############################################################################################################
