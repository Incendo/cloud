#!/bin/bash

export MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
./mvnw -T1C clean package -DskipTests=true
