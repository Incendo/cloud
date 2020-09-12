#!/bin/bash

# Make sure all submodules are initialized
git submodule update --remote
# Package all jars
./mvnw clean package
