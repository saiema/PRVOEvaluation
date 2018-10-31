#!/bin/bash

#set -x

#Documentation                                                          #
#Runs evosuite and/or randoop to generate tests and then it runs muJava
#
#Arguments
propertiesTemplateFile="$1"
budget="$2"
seed="$3"
resultsFolder="$4"
evoCriterion="branch:weakmutation"
#
#Constants
CURRENT_DIR=$(pwd)
#JUNIT, EVOSUITE, AND RANDOOP
EVOSUITE_JAR="${CURRENT_DIR}/tools/evosuite/evosuite-1.0.6.jar"
RANDOOP_JAR="${CURRENT_DIR}/tools/randoop/randoop-all-4.0.4.jar"
JUNIT="${CURRENT_DIR}/tools/junit-4.12.jar"
HAMCREST="${CURRENT_DIR}/tools/org.hamcrest.core_1.3.0.v201303031735.jar"
TESTING_JARS_ES="${CURRENT_DIR}/tools/evosuite/evosuite-standalone-runtime-1.0.6.jar"
TESTING_JARS_RD="${CURRENT_DIR}/tools/randoop/randoop-4.0.4.jar"
ES_JUNIT_SUFFIX="ESTest"
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
#JACOCO
JACOCO_AGENT="JaCoCoRS/jacocoagent.jar"
JACOCO_CLI="JaCoCoRS/jacococli.jar"
JACOCO_REPORT="JaCoCoRS/JaCoCoRS.sh"
#
#########################################################################


pushd () {
    command pushd "$@" > /dev/null
}

popd () {
    command popd "$@" > /dev/null
}

#Runs evosuite for the given arguments:
#class 			: class for which to generate tests
#project classpath 	: classpath to project related code and libraries
#output dir		: where tests will be placed
#criterion 		: the criteria that evosuite will use to evolve the test suite (weakmutation, strongmutation, branch, etc)
#budget 		: the time budget (in seconds) for evosuite
#seed 			: the seed to be used for the random generator
function evosuite() {
	echo "Running EvoSuite..."
	#-base_dir $outputDir 
	local class="$1"
	local projectCP="$2"
	local outputDir="$3"
	local criterion="$4"
	local budget="$5"
	local seed="$6"
	java -jar $EVOSUITE_JAR -class $class -projectCP $projectCP -Dtest_dir="$outputDir" -criterion $criterion -Djunit_suffix="$ES_JUNIT_SUFFIX" -Dsearch_budget=$budget -seed $seed
}

#Compiles evosuite generated tests
#target		: class to compile
#root folder	: folder to where the tests where saved
#classpath	: required classpath
#exitCode(R)	: where to return the exit code
function compileEvosuiteTests() {
	echo "Compiling EvoSuite tests..."
	local target="$1"
	local rootFolder="$2"
	local classpath="$3"
	pushd $rootFolder
	local exitCode=""
	javac -cp "$classpath:$JUNIT:$TESTING_JARS_ES" "$target"
	exitCode="$?"
	popd
	eval "$4=$exitCode"	
}

#Runs randoop for the given arguments:
#class			: class for which to generate tests
#project classpath	: classpath to project related code and libraries
#output dir		: where tests will be placed
#budget			: the time bugdet (in seconds) for randoop
#seed			: the seed to be used for the random generator
function randoop() {
	echo "Running Randoop..."
	local class="$1"
	local projectCP="$2"
	local outputDir="$3"
	local budget="$4"
	local seed="$5"
	local options="--flaky-test-behavior=DISCARD --time-limit=$budget --randomseed=$seed --junit-output-dir=$outputDir"
	java -Xmx3000m -cp $projectCP:$RANDOOP_JAR randoop.main.Main gentests --testclass="$class" $options
}

#Compiles randoop regression tests
#root folder	: folder where tests are
#classpath	: required classpath
#exitCode(R)	: where to return the exit code
function compileRandoopTests() {
	echo "Compiling Randoop tests..."
	local rootFolder="$1"
	local classpath="$2"
	pushd $rootFolder
	local exitCode=""
	javac -cp "$classpath:$JUNIT:$TESTING_JARS_RD" *.java
	exitCode="$?"
	popd
	eval "$3=$exitCode"
}

function getTestsFrom() {
	local from="$1"
	#local es_tests=$(find "$from" | awk '/\.java/' | awk "/${ES_JUNIT_SUFFIX}/")
	#local rp_tests=$(find "$from" | awk '/\.java/' | awk "/\/RegressionTest/")
	local es_tests=""
	local rp_tests=""
	local backupIFS="$IFS"
	IFS='
'
	[ -e "${from}" ] && for x in `find "${from}" | awk "/${ES_JUNIT_SUFFIX}\.java/"`; do
		testClass=$(echo $x | sed "s|${from}||g" | sed "s|\.java||g" | sed "s|\/|.|g") 
		if [ -z "$es_tests" ] ; then
			es_tests="$testClass"
		else
			es_tests="${es_tests} $testClass"
		fi		
		echo $testClass
	done
	if [ -e "${from}" ] && [ -e "${from}RegressionTest.java" ] ; then
		testClass="RegressionTest"
		if [ -z "$rp_tests" ] ; then
			rp_tests="$testClass"
		else
			rp_tests="${rp_tests} $testClass"
		fi		
		echo $testClass
	fi
	
	local all_tests=""
	if [ -z "$es_tests" ] ; then
		all_tests="$rp_tests"
	elif [ ! -z "$rp_tests" ] ; then
		all_tests="$es_tests $rp_tests"
	else
		all_tests="$es_tests"	
	fi
	eval "$2='$all_tests'"
	IFS="$backupIFS"
}

#Takes a *_template.properties and produce a .properties with a set of tests
#template		: the _template.properties
#tests			: tests as class names, separated by a space
#propertiesFile(R)	: the new .properties file
function makeCompletePropertiesFile() {
	echo "Generating complete properties file..."
	local template="$1"
	local tests="$2"
	local pFile=$(echo "$template" | sed 's/\_template\.properties/.properties/g')
	[ -e $pFile ] && exit 401
	cp "$template" "$pFile"
	sed -i "s|<TESTS>|${tests}|g" "$pFile"
	ecode="$?"
	[[ "$ecode" -ne "0" ]] && exit 402
	eval "$3='$pFile'"
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

#Runs jacoco for coverage, and generates a report
#classpath	: the classpath to use
#testpath	: the classpath to tests
#sourcefiles	: the source files of classes to cover
#tests		: tests to run
#classToAnalyze	: the class to analyze
function jacoco() {
	echo "Running JaCoCo..."
	local classpath="$1"
	local testpath="$2"
	local sourcefiles="$3"
	local tests="$4"
	local classToAnalyze="$5"
	local classToAnalyzeAsPath=$(echo "$classToAnalyze" | sed 's|\.|/|g')
	java -javaagent:"$JACOCO_AGENT" -cp "$classpath:$testpath:$JUNIT:$HAMCREST:$TESTING_JARS_ES:$TESTING_JARS_RD" org.junit.runner.JUnitCore $tests
	[[ "$?" -ne "0" ]] && exit 501
	java -jar "$JACOCO_CLI" report "jacoco.exec" --classfiles "${classpath}${classToAnalyzeAsPath}.class" --sourcefiles "${sourcefiles}${classToAnalyzeAsPath}.java" --xml "jacoco.report.xml"
	[[ "$?" -ne "0" ]] && exit 502
	sed -i 's;<!DOCTYPE report PUBLIC "-//JACOCO//DTD Report 1.1//EN" "report.dtd">;;g' "jacoco.report.xml"
	$JACOCO_REPORT "jacoco.report.xml" --class "$classToAnalyze" >"jacoco.report.resumed"
}

#Collect and move results
#outputDir		: the output folder where to collect results
#propertiesFile		: the used properties file
#budgetUsed		: the used budget
#seedUsed		: the used seed
function collectResults() {
	echo "Collecting results..."
	local resultsFolder="$1"
	local propertiesFile="$2"
	propertiesFileFolder=${propertiesFile##*/}
	propertiesFileFolder=${propertiesFileFolder%.*}
	local budgetUsed="$3"
	local seedUsed="$4"
	local outputDir="${resultsFolder}/${propertiesFileFolder}-${budgetUsed}_${seedUsed}"
	subsumptionOutput=$(grep -o "mutation.advanced.dynamicSubsumption.output= .*$" $propertiesFile)
	[ -z "$subsumptionOutput" ] && exit 701
	subsumptionOutput=$(echo "$subsumptionOutput" | sed 's/mutation\.advanced\.dynamicSubsumption\.output= //g')
	mkdir -p "$outputDir/subsumptionOutput/"
	mv "$propertiesFile" "$outputDir"
	mv "${propertiesFile}.out" "$outputDir"
	mv "${propertiesFile}.err" "$outputDir"	
	mv "jacoco.exec" "$outputDir"
	mv "jacoco.report.xml" "$outputDir"
	mv "jacoco.report.resumed" "$outputDir"	
	mv "externalOutput.log" "$outputDir"
	mv "externalError.log" "$outputDir"
	mv "$subsumptionOutput/"* "$outputDir/subsumptionOutput/"
}

#MAIN
[ ! -e $propertiesTemplateFile ] && exit 101
[[ ! $propertiesTemplateFile == *\.properties ]] && exit 102
[[ ! $propertiesTemplateFile == *\_template.properties ]] && exit 103
classname=$(grep -o "mutation.basic.class= .*$" $propertiesTemplateFile)
binDir=$(grep -o "path.original.bin= .*$" $propertiesTemplateFile)
testDir=$(grep -o "path.tests.bin= .*$" $propertiesTemplateFile)
sourceDir=$(grep -o "path.original.source= .*$" $propertiesTemplateFile)
mutantDir=$(grep -o "path.mutants= .*$" $propertiesTemplateFile)
[ -z "$classname" ] && exit 104
[ -z "$binDir" ] && exit 105
[ -z "$testDir" ] && exit 106
[ -z "$sourceDir" ] && exit 107
[ -z "$mutantDir" ] && exit 109
classname=$(echo "$classname" | sed 's/mutation\.basic\.class= //g')
classnameAsPath=$(echo "$classname" | sed 's;\.;/;g')
binDir=$(echo "$binDir" | sed 's/path\.original\.bin= //g')
testDir=$(echo "$testDir" | sed 's/path\.tests\.bin= //g')
sourceDir=$(echo "$sourceDir" | sed 's/path\.original\.source= //g')
mutantDir=$(echo "$mutantDir" | sed 's/path\.mutants= //g')
(grep --quiet "<TESTS>" $propertiesTemplateFile) || exit 110
[ -e "${mutantDir}" ] && [ -n "$(ls -A ${mutantDir})" ] && exit 111
[ -e "${testDir}" ] && [ -n "$(ls -A ${testDir})" ] && exit 112
[ ! -e "${testDir}" ] && mkdir -p "$testDir"
#TEST GENERATION
evosuite "$classname" "${CURRENT_DIR}/$binDir" "${CURRENT_DIR}/$testDir" "$evoCriterion" "$budget" "$seed"
ecode="$?"
[[ "$ecode" -ne "0" ]] && exit 201
randoop "$classname" "${CURRENT_DIR}/$binDir" "${CURRENT_DIR}/$testDir" "$budget" "$seed"
ecode="$?"
[[ "$ecode" -ne "0" ]] && exit 202
#TEST COMPILATION
compileEvosuiteTests "$classnameAsPath${ES_JUNIT_SUFFIX}.java" "${CURRENT_DIR}/${testDir}" "${CURRENT_DIR}/${binDir}:${CURRENT_DIR}/${testDir}" ecode
[[ "$ecode" -ne "0" ]] && exit 301
compileRandoopTests "${CURRENT_DIR}/${testDir}" "${CURRENT_DIR}/${binDir}:${CURRENT_DIR}/${testDir}" ecode
[[ "$ecode" -ne "0" ]] && exit 302

tests=""
getTestsFrom "${CURRENT_DIR}/$testDir" tests

jacoco "${CURRENT_DIR}/$binDir" "${CURRENT_DIR}/$testDir" "${CURRENT_DIR}/$sourceDir" "$tests" "$classname"

propertiesFile=""
makeCompletePropertiesFile "$propertiesTemplateFile" "$tests" propertiesFile

mujava "${CURRENT_DIR}/${binDir}:${CURRENT_DIR}/${testDir}:${JUNIT}:${TESTING_JARS_ES}:${TESTING_JARS_RD}" $propertiesFile
ecode="$?"
[[ "$ecode" -ne "0" ]] && exit 601

collectResults "$resultsFolder" "$propertiesFile" "$budget" "$seed"
rm -rf "evosuite-report"
rm -rf "error.log"


set -x
