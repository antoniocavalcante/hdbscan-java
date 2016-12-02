#!/bin/sh

DIR="data#6"
SUFFIX=".data"

for d in 2 4 8 16 32 64 128;
do
    for file in "${DIR}"/${d}d-*;
    do
        LAST=$(head -1 ${file} | sed 's/[^ ]//g' | wc -c)
        # echo ${LAST}
        cut -f ${LAST} -d' ' ${file} >> ${file%${SUFFIX}}.labels
        mv ${file} ${file}.old
        cut --complement -f ${LAST} -d' ' ${file}.old > ${file}
        rm ${file}.old
    done
done
