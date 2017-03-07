#!/bin/sh

DIR="data#6"

# rm -rf *.results

name() {
    output="ihdbscan"-$1

    if [ $2 = true ]; then
        output=$output-"smart"
    fi

    if [ $3 = true ]; then
        output=$output-"naive"
    fi

    if [ $2 = true ] || [ $3 = true ]; then
        if [ $4 = true ]; then
            output=$output-"incremental"
        fi
    fi

    output=$output".results"
}

dataset() {

    name "dataset" $1 $2 $3

    for n in 16 32 64 128 256 512 1024;
    do
		for i in $(seq 1)
		do
            # IHDBSCAN
            # args: dataset, minpoints, run, outputfiles, smartFilter, naiveFilter, incremental
            java -jar -Xmx61g -XX:+UseConcMarkSweepGC -XX:+UseParNewGC IHDBSCAN.jar "${DIR}/16d-${n}.dat" 16 $i $1 $2 $3 >> $output

            # HDBSCAN
            # java -jar -Xmx60g HDBSCAN.jar "${DIR}/16d-${n}.dat" 16 $i false >> hdbscan-dataset.results
        done
    done
}

minpoints() {

    name "minpoints" $1 $2 $3

    for minpoints in 2 4 8 16 32 64 128;
    do
		for i in $(seq 1)
		do
            # IHDBSCAN
            # args: dataset, minpoints, run, outputfiles, smartFilter, naiveFilter, incremental
	        java -jar -Xmx61g -XX:+UseConcMarkSweepGC -XX:+UseParNewGC IHDBSCAN.jar "${DIR}/16d-128.dat" ${minpoints} $i $1 $2 $3 >> $output

            # HDBSCAN
            # java -jar -Xmx60g HDBSCAN.jar "${DIR}/16d-128.dat" ${minpoints} $i false >> hdbscan-minpoints.results
        done
    done
}

dimensions() {

    name "dimensions" $1 $2 $3

    for d in 2 4 8 16 32 64 128;
    do
		for i in $(seq 1)
		do
            # IHDBSCAN
            # args: dataset, minpoints, run, outputfiles, smartFilter, naiveFilter, incremental
	        java -jar -Xmx61g -XX:+UseConcMarkSweepGC -XX:+UseParNewGC IHDBSCAN.jar "${DIR}/${d}d-128.dat" 16 $i $1 $2 $3 >> $output

            # HDBSCAN
            # java -jar -Xmx60g HDBSCAN.jar "${DIR}/${d}d-128.dat" 16 $i false >> hdbscan-dimensions.results
        done
    done
}

# NO FILTER
smartFilter="false"
naiveFilter="false"
incremental="false"

minpoints $smartFilter $naiveFilter $incremental
dimensions $smartFilter $naiveFilter $incremental
dataset $smartFilter $naiveFilter $incremental

# -----------------------------------------------------

# SMART INCREMENTAL
smartFilter="true"
naiveFilter="false"
incremental="true"

minpoints $smartFilter $naiveFilter $incremental
dimensions $smartFilter $naiveFilter $incremental
dataset $smartFilter $naiveFilter $incremental

# SMART + NAIVE INCREMENTAL
smartFilter="true"
naiveFilter="true"
incremental="true"

minpoints $smartFilter $naiveFilter $incremental
dimensions $smartFilter $naiveFilter $incremental
dataset $smartFilter $naiveFilter $incremental

# NAIVE INCREMENTAL
smartFilter="false"
naiveFilter="true"
incremental="true"

minpoints $smartFilter $naiveFilter $incremental
dimensions $smartFilter $naiveFilter $incremental
dataset $smartFilter $naiveFilter $incremental

# -----------------------------------------------------

# SMART
smartFilter="true"
naiveFilter="false"
incremental="false"

minpoints $smartFilter $naiveFilter $incremental
dimensions $smartFilter $naiveFilter $incremental
dataset $smartFilter $naiveFilter $incremental

# SMART + NAIVE
smartFilter="true"
naiveFilter="true"
incremental="false"

minpoints $smartFilter $naiveFilter $incremental
dimensions $smartFilter $naiveFilter $incremental
dataset $smartFilter $naiveFilter $incremental

# NAIVE
smartFilter="false"
naiveFilter="true"
incremental="false"

minpoints $smartFilter $naiveFilter $incremental
dimensions $smartFilter $naiveFilter $incremental
dataset $smartFilter $naiveFilter $incremental
