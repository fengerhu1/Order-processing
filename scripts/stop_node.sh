#!/bin/bash
USERNAME=centos
MASTER_HOST="vm1"
HOSTS="vm1 vm2 vm3 vm4"

#stop kafka
STOP_KAFKA="jps | grep 'Kafka' | awk -F ' ' '{print $1}' | xargs kill -9"

for HOSTNAME in ${HOSTS} ; do
    ssh ${HOSTNAME} "${STOP_KAFKA}"
	echo "Stop kafka on ${HOSTNAME}"
    sleep 1
done

#stop zookeeper
STOP_ZOOKEEPER="jps | grep 'QuorumPeerMain' | awk -F ' ' '{print $1}' | xargs kill -9"

for HOSTNAME in ${HOSTS} ; do
    ssh ${HOSTNAME} "${STOP_KAFKA}"
	echo "Stop zookeeper on ${HOSTNAME}"
    sleep 1
done

#stop spark
STOP_SPARK_SCRIPT="cd && cd spark-2.4.3-bin-hadoop2.7/sbin && ./stop-all.sh"
ssh ${MASTER_HOST} "${STOP_SPARK_SCRIPT}"

sleep 1

#stop hadoop
STOP_HADOOP_SCRIPT="cd /usr/local/hadoop/hadoop-3.1.2/sbin && ./stop-yarn.sh; ./stop-dfs.sh"
ssh ${MASTER_HOST} "${STOP_HADOOP_SCRIPT}"

sleep 1

