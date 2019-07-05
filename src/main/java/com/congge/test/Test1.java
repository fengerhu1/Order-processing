package com.congge.test;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class Test1 {

	private static final String CONNECT_ADDRESS = "127.0.0.1:2181";

	private static final int SESSION_TIMEOUT = 5000;
	
	//原子计数器，阻塞主线程
	private static final CountDownLatch countDown = new CountDownLatch(1);

	public static void main(String[] args) {

		ZooKeeper zooKeeper = null;
		try {
			zooKeeper = new ZooKeeper(CONNECT_ADDRESS, SESSION_TIMEOUT, new Watcher() {

				public void process(WatchedEvent event) {
					//获取事件状态
					KeeperState keeperState = event.getState();
					//获取事件类型
					EventType eventType = event.getType();
					
					if(KeeperState.SyncConnected == keeperState){
						//表示事件类型是当前节点还不存在的时候
						if(EventType.None == eventType){
							countDown.countDown();
							System.out.println("zookeeper启动连接成功.....");
						}
						if(EventType.NodeCreated == eventType){
							System.out.println("zookeeper 接收到事件通知，正在创建节点.....");
						}
						
					}
				}
			});
			
			//节点没有创建完毕，主线程阻塞
			countDown.await();
			
			String path = "/temp/test0001";
			zooKeeper.exists(path, true);	//允许接收节点创建成功时的通知
			
			//CreateMode.EPHEMERAL   临时节点
			//CreateMode.PERSISTENT  持久节点
			//Ids.OPEN_ACL_UNSAFE		允许所有客户端以非验证的方式连接
			
			String nodeResult = zooKeeper.create(path, "acong".getBytes(), Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL);
			
			System.out.println("节点创建成功:" + nodeResult);
			
			Thread.currentThread().sleep(10000);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (zooKeeper != null) {
				try {
					zooKeeper.close();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
