#!/usr/bin/env bash
if [ $1 = "start" ]; then
    # clean target for new build
    rm target -r
    # build project
    mvn package
    # start project
    java -jar target/*.jar > logs.txt &
fi
if [ $1 = "stop" ]; then
    # is not true, but in range this service it's ok
    killall java
fi