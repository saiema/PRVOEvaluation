#!/bin/bash


count_seeds=$1
outputfile=$2
if [ -e $outputfile ]; then
    rm $outputfile
fi
for ((k=1;k<=$count_seeds;k++)); do
   seed=$(( ( RANDOM % 10000 ) +1000 ))

   echo $seed >> $outputfile
done



