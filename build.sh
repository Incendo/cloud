#!/bin/bash

# Make sure all submodules are initialized
git submodule update --remote
# Package all jars

export MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
./mvnw -T1C clean package -DskipTests=true
