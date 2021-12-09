#!/bin/bash
sleep 5m
cd /home/ubuntu/webapp/webapp
sudo su
touch application.log
sudo chmod 777 application.log
sudo mvn clean
sudo mvn clean install
(sudo java -jar target/*.jar . &) > application.log 2>&1
sleep 1m