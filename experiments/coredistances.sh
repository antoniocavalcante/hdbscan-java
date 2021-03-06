#!/bin/sh

DIR="data#6"

dataset() {
    for n in 16 32 64 256 512 1024;
    do
        java -jar coredistances.jar "${DIR}/16d-${n}.dat" 16 true >> core-distances-kd-tree.results.2
    done
}

minpoints() {
    java -jar coredistances.jar "${DIR}/16d-128.dat" 128 true >> core-distances-kd-tree.results.2
}

dimensions() {
    for d in 2 4 8 16 32 64 128;
    do
        java -jar coredistances.jar "${DIR}/${d}d-128.dat" 128 true >> core-distances-kd-tree.results.2
    done
}

#dataset
#minpoints
dimensions
