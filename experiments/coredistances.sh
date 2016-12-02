#!/bin/sh

DIR="data#6"

# rm -rf *.results

dataset() {
    for n in 16 32 64 256 512 1024;
    do
        java -jar coredistances.jar "${DIR}/16d-${n}.dat" 16
    done
}

minpoints() {
    java -jar coredistances.jar "${DIR}/16d-128.dat" 128
}

dimensions() {
    for d in 2 4 8 32 64 128;
    do
        java -jar coredistances.jar "${DIR}/${d}d-128.dat" 16
    done
}

option="${1}" 

case ${option} in
   1) echo "dimensions"
      dimensions
      ;; 
   2) echo "dataset"
      dataset
      ;;
   3) echo "minpoints"
      minpoints
      ;; 
esac
