#!/bin/bash
sleep 10s
sudo su
cd /home/ubuntu/webapp/
TEMPVAR=$(lsof -i tcp:8080 | grep "java" | awk '{print $2}')

if [ -z "$TEMPVAR" ]
then
    echo "\$TEMPVAR is empty"
else
    kill -9 $TEMPVAR
fi