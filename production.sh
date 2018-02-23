#!/usr/bin/env bash

# Update it is like consecutive execution 'stop', 'pull', 'build', 'start'
if [ $1 = "update" ]; then
    # is not true, but in range this service it's ok
    killall java
    # pull last changes from github
    git pull origin master
    # clean target for new build
    rm target -r
    # build project
    mvn package
    # start project
    java -jar target/*.jar > logs-$(date +%d.%m.%Y-%Hh.%Mm.%Ss).txt &
fi

if [ $1 = "build" ]; then
    # clean target for new build
    rm target -r
    # build project
    mvn package
fi

if [ $1 = "pull" ]; then
    git pull origin master
fi

if [ $1 = "start" ]; then
    # start project
    java -jar target/*.jar > logs-$(date +%d.%m.%Y-%Hh.%Mm.%Ss).txt &
fi

if [ $1 = "stop" ]; then
    # is not true, but in range this service it's ok
    killall java
fi

if [ $1 = "stop" ]; then
    # is not true, but in range this service it's ok
    killall java
fi