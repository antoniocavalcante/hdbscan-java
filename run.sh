#!/bin/bash

# Updates the project to the latest version from Git.
git fetch --all
git reset --hard origin/master

# Compiles the source files and generates the jar files.
# source compile.sh

# Changes to the experiments directory.
cd experiments

# Runs experiments.
source experiments.sh
