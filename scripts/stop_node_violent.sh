#!/bin/bash
MASTER_HOST="vm1"
SLAVE_HOSTS="vm2 vm3 vm4"
HOSTS="vm1 vm2 vm3 vm4"

#stop spark
STOP_SPARK_MASTER="jps | grep 'Master' | awk -F ' ' '{print $1}' | xargs kill -9"
STOP_SPARK_WORKER="jps | grep 'Worker' | awk -F ' ' '{print $1}' | xargs kill -9"

for SLAVE in ${HOSTS} ; do
    ssh ${SLAVE} "${STOP_SPARK_WORKER}"
	echo "Stop spark worker on ${SLAVE}"
    sleep 1
done

ssh ${MASTER_HOST} "${STOP_SPARK_MASTER}"
echo "Stop spark master on ${MASTER_HOST}"

#stop hadoop
STOP_HADOOP_NAMENODE="jps | grep 'NameNode' | awk -F ' ' '{print $1}' | xargs kill -9;
					  jps | grep 'ResourceManager' | awk -F ' ' '{print $1}' | xargs kill -9;
					  jps | grep 'SecondaryNameNode' | awk -F ' ' '{print $1}' | xargs kill -9;"

STOP_HADOOP_DATANODE="jps | grep 'DataNode' | awk -F ' ' '{print $1}' | xargs kill -9;
					  jps | grep 'NodeManager' | awk -F ' ' '{print $1}' | xargs kill -9;"


while true;
do
	read -r -p "Stop hadoop? [yes/no] " input

	case $input in
	    [yY][eE][sS]|[yY])
			echo "Stop hadoop..."
			for SLAVE in ${HOSTS} ; do
				ssh ${SLAVE} "${STOP_HADOOP_DATANODE}"
				echo "Stop hadoop datanode on ${SLAVE}"
				sleep 1
			done

			ssh ${MASTER_HOST} "${STOP_HADOOP_NAMENODE}"
			echo "Stop hadoop namenode on ${MASTER_HOST}"
			break
			;;

	    [nN][oO]|[nN])
			echo "Don't stop hadoop."
			break
	       		;;

	    *)
		echo "Invalid input..."
		;;
	esac
done;

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
