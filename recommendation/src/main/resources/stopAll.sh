#!/bin/bash

echo "stop zookeeper"
/usr/local/kafka/bin/zookeeper-server-stop.sh

echo "stop hbase"
hbasePid=$(pgrep -f HMaster)
kill -s 9 $hbasePid

echo "stop kafka server"
# rm -rf /usr/local/kafka/data
/usr/local/kafka/bin/kafka-server-stop.sh
echo "stop flink"
/mnt/d/Program/flink-1.9.0/bin/stop-cluster.sh

echo "stop kafka producer"
pid=$(pgrep -f "generator.sh")
kill -s 9 $pid

echo "stop redis"
redis=$(pgrep -f redis)
kill -s 9 $redis