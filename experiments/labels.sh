#!/bin/sh

DIR="data#6"
SUFFIX=".data"

for file in "${DIR}"/*;
do
    LAST=$(head -1 ${file} | sed 's/[^ ]//g' | wc -c)
    # echo ${LAST}
    cut -f ${LAST} -d' ' ${file} >> ${file%${SUFFIX}}.labels
    mv ${file} ${file}.old
    cut --complement -f ${LAST} -d' ' ${file}.old > ${file}
    rm ${file}.old
done
