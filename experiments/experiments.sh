#!/bin/sh

DIR="/home/toni/git/HDBSCAN_Star/experiments/data#2"

rm -rf *.results

for d in 2 #3 4 5 6 7 8 9 10;
do
    for c in 2 #4 8 16 32 64 128;
    do
    	for minPoints in 100;
    	do
    		for i in $(seq 1)
    		do
    			java -jar -Xmx2g -Xmx7g Incremental_HDBSCAN_Star.jar "${DIR}/${d}d-${c}c-no0.dat" ${minPoints} >> incremental_hdbscan_star.results
                # java -jar HDBSCAN_Star.jar "${DIR}/${d}d-${c}c-no0.dat" ${minPoints} >> hdbscan_star.results
    		done
    	done
    done
done

# for d in 2 #3 4 5 6 7 8 9 10;
# do
#     for c in 2 4 8 16 32 64 128;
#     do
#     	for minPoints in 10 20 30 40 50 60 70 80 90 100;
#     	do
#     		for i in $(seq 1)
#     		do
#     			java -jar HDBSCAN_Star.jar "${DIR}/${d}d-${c}c-no0.dat" ${minPoints} >> hdbscan_star_results
#     		done
#     	done
#     done
# done
