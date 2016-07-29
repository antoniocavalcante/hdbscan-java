#!/bin/bash

source update.sh

# Compiles the source files and generates the jar files.
source compile.sh

# Changes to the experiments directory.
cd experiments

# Runs experiments.
source experiments.sh
