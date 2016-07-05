#!/bin/sh

DIR="data#2"

rm -rf *.results

ihdbscan-no-filter() {
    for d in 2 3 #4 5 6 7 8 9 10;
    do
        for c in 2 4 8 16 32 #64 128;
        do
        	for minPoints in 10 20 30 40 50 60 70 80 90 100;
        	do
        		for i in $(seq 1)
        		do
        			java -jar -Xmx2g -Xmx7g IHDBSCAN.jar "${DIR}/${d}d-${c}c-no0.dat" ${minPoints} 1 false WS >> ${minPoints}-ihdbscan.results
        		done
        	done
        done
    done
}

ihdbscan-filter() {
    for d in 2 3 #4 5 6 7 8 9 10;
    do
        for c in 2 4 8 16 32 #64 128;
        do
        	for minPoints in 10 20 30 40 50 60 70 80 90 100;
        	do
        		for i in $(seq 1)
        		do
        			java -jar -Xmx2g -Xmx7g IHDBSCAN.jar "${DIR}/${d}d-${c}c-no0.dat" ${minPoints} 1 true WS >> ${minPoints}-ihdbscan.results
        		done
        	done
        done
    done
}

hdbscan() {
    for d in 2 3 #4 5 6 7 8 9 10;
    do
        for c in 2 4 8 16 32 #64 128;
        do
        	for minPoints in 10 20 30 40 50 60 70 80 90 100;
        	do
        		for i in $(seq 1)
        		do
        			java -jar -Xmx2g -Xmx7g HDBSCAN.jar "${DIR}/${d}d-${c}c-no0.dat" ${minPoints} >> ${minPoints}-hdbscan.results
        		done
        	done
        done
    done
}

ihdbscan-no-filter
ihdbscan-filter
hdbscan
