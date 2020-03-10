#!/bin/bash

if [ $# -lt 2  ]
  then
    echo "No arguments supplied. Query size and path are needed."
    exit 1
fi

value=$1
path=$2

cd $path

echo "algo;thres;run;usertime;systime;percent CPU;wallclocktime(h:mm:ss/mm:ss);tooltime1;tooltime2;tooltime3" > times_$value.csv

find -type d -iname "*_*_${value}_*_*" -print0 | sort -z |
while IFS= read -r -d '' line; do
    algo=`echo $line | sed "s/\.\/\([^0-9]*\)_.*/\1/"`

    thres=`echo $line | sed "s/.*_${value}_\([0-9]*\.[0-9]*\)_.*/\1/"`

    run=`echo $line | sed "s/.*_\([0-9]*\)$/\1/"`

    usertime=`grep "User time (seconds)" "$line/log.txt" | sed "s/[^0-9]*\([0-9]*\.[0-9]*\).*/\1/"`
    usertime=`echo "$usertime/60" | bc -l`

    systime=`grep "System time (seconds)" "$line/log.txt" | sed "s/[^0-9]*\([0-9]*\.[0-9]*\).*/\1/"`
    systime=`echo "$systime/60" | bc -l`

    cpu=`grep "Percent of CPU this job got" "$line/log.txt" | sed "s/[^0-9]*\([0-9]*\).*/\1%/"`

    wallclocktime=`grep "Elapsed (wall clock) time (h:mm:ss or m:ss)" "$line/log.txt" | sed "s/[^0-9]*\([0-9]*:[0-9]*.*\).*/\1/"`

    tooltimes=`sed -n "s/.*,\([0-9][0-9eE]*\)/\1/p" "$line/$line.csv"`
    temp=`perl -pe 's/([-\d.]+)e(?:\+|(-))?(\d+)/$1*10^$2$3/gi' <<<"$tooltimes" | sed ':a;N;$!ba;s/\n/\/1000\/60;/g'`
    tooltimes=`echo "$temp/1000/60" | bc -l | sed ':a;N;$!ba;s/\n/;/g'`

    echo "$algo;$thres;$run;$usertime;$systime;$cpu;$wallclocktime;$tooltimes" >> times_$value.csv
done

cat times_$value.csv

