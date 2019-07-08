package rwlock;

import org.I0Itec.zkclient.ZkClient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 读锁
 *
 * @author jimmie
 */
public class ReadLock extends BaseLock implements LockI {

    /** 锁名称前缀 */
    public static final String LOCK_NAME = "read-lock-";

    /** Zookeeper中locker节点的路径，如：/locker */
    private final String basePath;

    /** 获取锁以后自己创建的那个顺序节点的路径 */
    private String ourLockPath;

    public ReadLock(ZkClient client, String basePath) {
        super(client, basePath, LOCK_NAME);
        this.basePath = basePath;
        if(!client.exists(basePath)) {
            client.createPersistent(basePath,true);
        }
    }

    @Override
    public void getLock() throws Exception {
        // -1 表示永不超时
        ourLockPath = tryGetLock(-1, null);
        System.out.println("已获取到锁，节点路径名为："+ourLockPath);
        if(ourLockPath == null){
            throw new IOException("连接丢失!在路径:'" + basePath + "'下不能获取锁!");
        }
    }

    @Override
    public boolean getLock(long timeOut, TimeUnit timeUnit) throws Exception {
        ourLockPath = tryGetLock(timeOut, timeUnit);
        return ourLockPath != null;
    }

    @Override
    public void releaseLock() throws Exception {
        releaseLock(ourLockPath);
    }

	@Override
	public boolean isGetTheLock(List<String> children, String ourPath) {
		int ourIndex = getOurIndex(children, ourPath);
		for(int i=0;i<ourIndex;i++){
			if(children.get(i).contains(WriteLock.LOCK_NAME))
				return false;
		}
		return true;
	}

	@Override
	public String getWatchPath(List<String> children, String ourPath) {
		int ourIndex = getOurIndex(children, ourPath);
		int watchIndex = 0;
		for(int i=0;i<ourIndex;i++){
			if(children.get(i).contains(WriteLock.LOCK_NAME))
				watchIndex = i;
		}
		
		String watchPath = basePath.concat("/").concat(children.get(watchIndex));
		System.out.println(Thread.currentThread().getName()+"===="+ourPath+"监听的节点为："+watchPath);
		return watchPath;
	}

}
