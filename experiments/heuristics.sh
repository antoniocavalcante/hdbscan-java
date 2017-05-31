#!/bin/sh

DIR="data#6"
numqueries=1000

dataset() {
    echo "DATASET"
    for n in 16 32 64 256 512 1024;
    do
        output="$(java -jar heuristics.jar "${DIR}/16d-${n}.dat" 16 $numqueries)"
        echo $output
    done

}

minpoints() {
    echo "MINPOINTS"
    for m in 2 4 8 32 64 128;
    do
        output="$(java -jar heuristics.jar "${DIR}/16d-128.dat" $m $numqueries)"
        echo $output
    done
}

dimensions() {
    echo "DIMENSIONS"
    for d in 2 4 8 32 64 128;
    do
        output="$(java -jar heuristics.jar "${DIR}/${d}d-128.dat" 16 $numqueries)"
        echo $output
    done
}

dataset
minpoints
dimensions
