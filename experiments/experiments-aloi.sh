#!/bin/sh

DIR="data#4"

# rm -rf *.results

ihdbscannofilter() {
	for file in "${DIR}"/*;
	do
		for minPoints in 100 #10 20 30 40 50 60 70 80 90 100;
                do
	  		java -jar -Xms2g -Xmx7g IHDBSCAN.jar "${file}" ${minPoints} 1 false WS >> aloi-${minPoints}-ihdbscan.results
		done
	done
}

ihdbscanfilter() {
        for file in "${DIR}"/*;
        do
                for minPoints in 100 #10 20 30 40 50 60 70 80 90 100;
                do
                        java -jar -Xms2g -Xmx7g IHDBSCAN.jar "${file}" ${minPoints} 1 true WS >> aloi-${minPoints}-ihdbscan-filter.results
                done
        done
}

hdbscan() {
        for file in "${DIR}"/*;
        do
                for minPoints in 100 #10 20 30 40 50 60 70 80 90 100;
                do
                        java -jar -Xms2g -Xmx7g HDBSCAN.jar "${file}" ${minPoints} >> aloi-${minPoints}-hdbscan.results
                done
        done
}

mstihdbscan() {
        for file in "${DIR}"/*;
        do
                for minPoints in 100 #10 20 30 40 50 60 70 80 90 100;
                do
                        java -jar -Xms2g -Xmx7g MSTIHDBSCAN.jar "${file}" ${minPoints} >> aloi-${minPoints}-mstihdbscan.results
                done
        done
}

#ihdbscannofilter
#ihdbscanfilter
#hdbscan
mstihdbscan
