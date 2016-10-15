#!/bin/sh

DIR="data#6"

# rm -rf *.results

dataset() {
    for c in 16 32 64 128 256 512 1024;
    do
		for i in $(seq 1)
		do
            # MSTIHDBSCAN
            # java -jar -Xms2g -Xmx7g MSTIHDBSCAN.jar "${DIR}/${d}d-${c}c-no0.dat" ${minPoints} $i false >> ${d}d-${minPoints}-mstihdbscan.results

            # IHDBSCAN
            java -jar -Xms2g -Xmx7g IHDBSCAN.jar "${DIR}/16d-${c}.dat" 16 $i false false >> ihdbscan-dataset-nofilter.results

            # HDBSCAN
            # java -jar -Xms2g -Xmx7g HDBSCAN.jar "${DIR}/16d-${c}.dat" 16 $i false >> hdbscan-dataset.results
        done
    done
}

minpoints() {
    for minpoints in 2 4 8 16 32 64 128;
    do
		for i in $(seq 1)
		do
            # MSTIHDBSCAN
            # java -jar -Xms2g -Xmx7g MSTIHDBSCAN.jar "${DIR}/${d}d-${c}c-no0.dat" ${minPoints} $i false >> ${d}d-${minPoints}-mstihdbscan.results

            # IHDBSCAN
	        java -jar -Xms2g -Xmx7g IHDBSCAN.jar "${DIR}/16d-128.dat" ${minPoints} $i false false >> ihdbscan-minpoints-nofilter.results

            # HDBSCAN
            # java -jar -Xms2g -Xmx7g HDBSCAN.jar "${DIR}/16d-128.dat" ${minpoints} $i false >> hdbscan-minpoints.results
        done
    done
}

dimensions() {
    for d in 4 8 16 32 64 128;
    do
		for i in $(seq 1)
		do
            # MSTIHDBSCAN
            # java -jar -Xms2g -Xmx7g MSTIHDBSCAN.jar "${DIR}/${d}d-${c}c-no0.dat" ${minPoints} $i false >> ${d}d-${minPoints}-mstihdbscan.results

            # IHDBSCAN
	        java -jar -Xms2g -Xmx7g IHDBSCAN.jar "${DIR}/${d}d-128.dat" 16 $i false false >> ihdbscan-dimensions-nofilter.results

            # HDBSCAN
            # java -jar -Xms2g -Xmx7g HDBSCAN.jar "${DIR}/${d}d-128.dat" 16 $i false >> hdbscan-dimensions.results
        done
    done
}

