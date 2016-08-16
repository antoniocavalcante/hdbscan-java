#!/bin/sh

DIR="data#3"

# rm -rf *.results

run() {
    for c in 2 4 8 16 32 64 128;
    do
        for d in 2 4 8 16 32 64 128;
        do
        	for minPoints in 10 20 30 40 50 60 70 80 90 100;
        	do
        		for i in $(seq 5)
        		do
                    # MSTIHDBSCAN
                    java -jar -Xms2g -Xmx7g MSTIHDBSCAN.jar "${DIR}/${d}d-${c}c-no0.dat" ${minPoints} $i false >> ${d}d-${minPoints}-mstihdbscan.results

                    # IHDBSCAN
        			java -jar -Xms2g -Xmx7g IHDBSCAN.jar "${DIR}/${d}d-${c}c-no0.dat" ${minPoints} $i false false >> ${d}d-${minPoints}-ihdbscan.results

                    # HDBSCAN
                    java -jar -Xms2g -Xmx7g HDBSCAN.jar "${DIR}/${d}d-${c}c-no0.dat" ${minPoints} $i false >> ${d}d-${minPoints}-hdbscan.results
        		done
        	done
        done
    done
}

run
