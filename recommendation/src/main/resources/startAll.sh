#!/bin/bash

echo "start zookeeper"
nohup /usr/local/kafka/bin/zookeeper-server-start.sh /usr/local/kafka/config/zookeeper.properties  > /usr/local/kafka/zookeeper.out  2>&1 &

echo "start hbase"
start-hbase.sh

echo "start kafka server"
rm -rf /usr/local/kafka/data
nohup /usr/local/kafka/bin/kafka-server-start.sh /usr/local/kafka/config/server.properties > /usr/local/kafka/kafka_server.out 2>&1 &

echo "start flink"
nohup /mnt/d/Program/flink-1.9.0/bin/start-cluster.sh > /mnt/d/Program/flink-1.9.0/flink.out 2>&1 &

# echo "start kafka producer1"
# nohup /home/jason/recommendSystem/resources/generator.sh > /home/jason/recommendSystem/resources/gen.out 2>&1 &

echo "start redis"
nohup /mnt/d/Program/redis/redis-server.exe redis.windows.conf > redis.out 2>&1 &

# echo "start kafka producer2"
# nohup /home/jason/recommendSystem/resources/generator2.sh > /home/jason/recommendSystem/resources/gen2.out 2>&1 &
