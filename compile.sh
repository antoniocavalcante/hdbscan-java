#!/bin/bash

# Removes the previous jar files.
echo "[$(date)] Removing previous jar files..."
rm -f *.jar

# Finds all the Java classes to be compiled.
echo "[$(date)] Finding the source files to be compiled..."
find -name "*.java" > sources.txt

# Creates bin directory, if it does not exists.
echo "[$(date)] Creating build directory, if one does not exist..."
[ -d "bin" ] && echo "[$(date)] Directory bin exists." || mkdir bin

# Compiles the files.
echo "[$(date)] Compiling the source files..."
javac -classpath "lib/*" -d bin @sources.txt

# Removes the file source.txt, that contains the list of the Java sources files.
echo "[$(date)] Removing temporary file sources.txt..."
rm -f sources.txt

# Creates jar file for Incremental HDBSCAN*.
echo "[$(date)] Creating jar file for Incremental HDBSCAN*..."
jar -cfm IHDBSCAN.jar IHDBSCAN.MF -C bin/ .

# Creates jar file for HDBSCAN*.
echo "[$(date)] Creating jar file for HDBSCAN*..."
jar -cfm HDBSCAN.jar IHDBSCAN.MF -C bin/ .

# Copies the jar files to the experiments directory.
echo "[$(date)] Copying the jar files to the experiments directory..."
cp *.jar experiments/

echo "[$(date)] DONE!"
