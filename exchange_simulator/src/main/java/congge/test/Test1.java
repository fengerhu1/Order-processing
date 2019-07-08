package congge.test;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

public class Test1 {

	private static final String CONNECT_ADDRESS = "127.0.0.1:2181";

	private static final int SESSION_TIMEOUT = 5000;
	
	//ԭ�Ӽ��������������߳�
	private static final CountDownLatch countDown = new CountDownLatch(1);

	public static void main(String[] args) {

		ZooKeeper zooKeeper = null;
		try {
			zooKeeper = new ZooKeeper(CONNECT_ADDRESS, SESSION_TIMEOUT, new Watcher() {

				public void process(WatchedEvent event) {
					//��ȡ�¼�״̬
					KeeperState keeperState = event.getState();
					//��ȡ�¼�����
					EventType eventType = event.getType();
					
					if(KeeperState.SyncConnected == keeperState){
						//��ʾ�¼������ǵ�ǰ�ڵ㻹�����ڵ�ʱ��
						if(EventType.None == eventType){
							countDown.countDown();
							System.out.println("zookeeper�������ӳɹ�.....");
						}
						if(EventType.NodeCreated == eventType){
							System.out.println("zookeeper ���յ��¼�֪ͨ�����ڴ����ڵ�.....");
						}
						
					}
				}
			});
			
			//�ڵ�û�д�����ϣ����߳�����
			countDown.await();
			
			String path = "/temp/test0001";
			zooKeeper.exists(path, true);	//������սڵ㴴���ɹ�ʱ��֪ͨ
			
			//CreateMode.EPHEMERAL   ��ʱ�ڵ�
			//CreateMode.PERSISTENT  �־ýڵ�
			//Ids.OPEN_ACL_UNSAFE		�������пͻ����Է���֤�ķ�ʽ����
			
			String nodeResult = zooKeeper.create(path, "acong".getBytes(), Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL);
			
			System.out.println("�ڵ㴴���ɹ�:" + nodeResult);
			
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
