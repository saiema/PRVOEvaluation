#!/bin/bash

#set -x

#Documentation
#Runs n repetitions of evaluation
#
#Arguments
propertiesTemplateFile="$1"
budget="$2"
seedsFile="$3"
from="$4"
to="$5"

for (( count=$from; count<=$to; count++ ))
do
	seed=$(sed "${count}q;d" "$seedsFile")
	./evaluate.sh "$propertiesTemplateFile" "$budget" "$seed" "results" "$count"	
	[ -e "mutants" ] && rm -rf "mutants"
	[ -e "tests" ] && rm -rf "tests"
	[ -e "dynamicSubsumption" ] && rm -rf "dynamicSubsumption"
done
