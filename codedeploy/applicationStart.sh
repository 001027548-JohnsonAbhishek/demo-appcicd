#!/bin/bash
sleep 5m
cd /home/ubuntu/webapp/webapp
sudo su
sudo mvn clean
sudo mvn clean install
(sudo java -jar target/*.jar . &)
sleep 1m