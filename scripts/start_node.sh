#!/bin/bash
USERNAME=centos
MASTER_HOST="vm1"
HOSTS="vm1 vm2 vm3 vm4"

#start zookeeper
START_ZOOKEEPER_SCRIPT="cd && cd kafka_2.12-2.2.0/bin; ./start_zk.sh"
for HOSTNAME in ${HOSTS} ; do
    ssh ${HOSTNAME} "${START_ZOOKEEPER_SCRIPT}"
	echo "Start zookeeper on ${HOSTNAME}"
    sleep 1
done

#start kafka
START_KAFKA_SCRIPT="cd && cd kafka_2.12-2.2.0/bin; ./start_kfk.sh"
for HOSTNAME in ${HOSTS} ; do
    ssh ${HOSTNAME} "${START_KAFKA_SCRIPT}"
	echo "Start kafka on ${HOSTNAME}"
	sleep 1
done

#start hadoop
START_HADOOP_SCRIPT="cd /usr/local/hadoop/hadoop-3.1.2/sbin && ./start-dfs.sh; ./start-yarn.sh"
ssh ${MASTER_HOST} "${START_HADOOP_SCRIPT}"

sleep 1

#start spark
START_SPARK_SCRIPT="cd && cd spark-2.4.3-bin-hadoop2.7/sbin && ./start-all.sh"
ssh ${MASTER_HOST} "${START_SPARK_SCRIPT}"

sleep 1


