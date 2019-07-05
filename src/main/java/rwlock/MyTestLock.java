package rwlock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

/**
 * 测试分布式锁
 *
 * @author jerome_s@qq.com
 * @date 2016/8/26 22:53
 */
public class MyTestLock {
	
	private static String basepath = "/locker";
	
	/** 启动的服务个数 */
	private static final int CLIENT_QTY = 3;
	
//	private static ExecutorService exec = Executors.newFixedThreadPool(CLIENT_QTY); 
	
	private static ExecutorService exec = Executors.newCachedThreadPool();
	
	static{
		
		ZkClient zkclient = new ZkClient("10.0.0.77:2181", 500000, 500000, new SerializableSerializer());
		zkclient.createPersistent(basepath,true);
	}

	public static class ThreadDemo extends Thread {

		private LockI lock;
		
		private String name;
		
		public ThreadDemo(String name,LockI lock){
			this.lock = lock;
			this.name = name;
			setName(name);
		}
		@Override
		public void run(){
			try {
				lock.getLock();
//				System.out.println(name+" is get lock!");
				 // 5s 后lock1释放锁
				System.out.println(name+" is sleeping 5s");
	            Thread.sleep(5000);
	            lock.releaseLock();
	            System.out.println(name+" is released lock");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
		}
		
	}
	
    public static void main(String[] args) {

        for(int i=0;i<CLIENT_QTY;i++){
        	   ZkClient zkClient4 = new ZkClient("10.0.0.77:2181,10.0.0.154:2181,10.0.0.137:2181", 5000, 5000, new BytesPushThroughSerializer());
               WriteLock lock4 = new WriteLock(zkClient4, basepath);
               exec.submit(new ThreadDemo("Thread-Write-"+i, lock4));
        }
        
        for(int i=0;i<CLIENT_QTY;i++){
     	   ZkClient zkClient4 = new ZkClient("10.0.0.77:2181,10.0.0.154:2181,10.0.0.137:2181", 5000, 5000, new BytesPushThroughSerializer());
            ReadLock lock4 = new ReadLock(zkClient4, basepath);
            exec.submit(new ThreadDemo("Thread-Read-"+i, lock4));
     }

    }

}
