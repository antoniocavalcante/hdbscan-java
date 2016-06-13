#!/bin/sh

DIR="data#2"

rm -rf *.results

for d in 2 #3 4 5 6 7 8 9 10;
do
    for c in 2 4 8 #16 32 64 128;
    do
    	for minPoints in 100;
    	do
    		for i in $(seq 1)
    		do
    			java -jar -Xmx2g -Xmx7g IHDBSCAN.jar "${DIR}/${d}d-${c}c-no0.dat" ${minPoints} >> ihdbscan.results
    		done
    	done
    done
done

for d in 2 #3 4 5 6 7 8 9 10;
do
    for c in 2 4 8 #16 32 64 128;
    do
    	for minPoints in 100;
    	do
    		for i in $(seq 1)
    		do
    			java -jar -Xmx2g -Xmx7g HDBSCAN.jar "${DIR}/${d}d-${c}c-no0.dat" ${minPoints} >> hdbscan.results
    		done
    	done
    done
done
