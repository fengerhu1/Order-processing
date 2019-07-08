
package rwlock;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁 基础类
 * 主要用于和Zookeeper交互
 *
 * @author jerome_s@qq.com
 * @date 2016/8/26 22:51
 */
public abstract class BaseLock {

    private final ZkClient client;
    private final String path;
    private final String basePath;
//    private final String lockName;
    private final String split = "-lock-";

    /** 重试获取锁次数 */
    private static final Integer MAX_RETRY_COUNT = 10;

    public BaseLock(ZkClient client, String basePath, String lockName) {
        this.client = client;
        this.basePath = basePath;
        this.path = basePath.concat("/").concat(lockName);
//        this.lockName = lockName;
    }

    /**
     * 等待获取锁
     * @param startMillis
     * @param millisToWait
     * @param ourPath
     * @return
     * @throws Exception
     */
    private boolean waitToLock(long startMillis, Long millisToWait, String ourPath) throws Exception {

        // 是否得到锁
        boolean haveTheLock = false;
        // 是否需要删除当前锁的节点
        boolean doDeleteOurPath = false;

        try {

            while (!haveTheLock) {

                // 获取所有锁节点(/locker下的子节点)并排序(从小到大)
                List<String> children = getSortedChildren();

//                System.out.println(Thread.currentThread().getName()+"==="+path+" 获取的节点顺序为："+children);
                boolean isGetTheLock = isGetTheLock(children,ourPath);//判断是否获取锁逻辑

                if (isGetTheLock) {
                    // 如果第一位 已经获得锁
                    haveTheLock = true;
                } else {
                    System.out.println(ourPath+"未获取到锁，开始设置监听=====");
                    String watchPath = getWatchPath(children,ourPath);//获取监听节点路径逻辑
                    
                    final CountDownLatch latch = new CountDownLatch(1);
                    final IZkDataListener previousListener = new IZkDataListener() {

                        public void handleDataDeleted(String dataPath) throws Exception {
                            latch.countDown();
                        }

                        public void handleDataChange(String dataPath, Object data) throws Exception {
                        }
                    };

                    try {
                        client.subscribeDataChanges(watchPath, previousListener);//只监听前一个节点

                        if (millisToWait != null) {
                            millisToWait -= (System.currentTimeMillis() - startMillis);
                            startMillis = System.currentTimeMillis();
                            if (millisToWait <= 0) {
                                doDeleteOurPath = true;
                                break;
                            }

                            latch.await(millisToWait, TimeUnit.MICROSECONDS);
                        } else {
                            latch.await();
                        }
                    } catch (ZkNoNodeException e) {
                        e.printStackTrace();
                    } finally {
                        client.unsubscribeDataChanges(watchPath, previousListener);
                    }

                }
            }
        } catch (Exception e) {
            //发生异常需要删除节点
            doDeleteOurPath = true;
            throw e;
        } finally {
            //如果需要删除节点
            if (doDeleteOurPath) {
                deleteOurPath(ourPath);
            }
        }

        return haveTheLock;
    }

	private String getLockNodeNumber(String str, String lockName) {
        int index = str.lastIndexOf(lockName);
        if (index >= 0) {
            index += lockName.length();
            return index <= str.length() ? str.substring(index) : "";
        }
        return str;
    }

    /**
     * 获取所有锁节点(/locker下的子节点)并排序
     *
     * @return
     * @throws Exception
     */
    private List<String> getSortedChildren() throws Exception {
        try {

            List<String> children = client.getChildren(basePath);
            Collections.sort
                    (
                            children,
                            new Comparator<String>() {
                                public int compare(String lhs, String rhs) {
                                    return getLockNodeNumber(lhs, split).compareTo(getLockNodeNumber(rhs, split));
                                }
                            }
                    );
            return children;

        } catch (ZkNoNodeException e) {
            client.createPersistent(basePath, true);
            return getSortedChildren();

        }
    }

    protected void releaseLock(String lockPath) throws Exception {
        deleteOurPath(lockPath);
    }

    /**
     * 尝试获取锁
     * @param timeOut
     * @param timeUnit
     * @return 锁节点的路径没有获取到锁返回null
     * @throws Exception
     */
    protected String tryGetLock(long timeOut, TimeUnit timeUnit) throws Exception {

        long startMillis = System.currentTimeMillis();
        Long millisToWait = (timeUnit != null) ? timeUnit.toMillis(timeOut) : null;

        String ourPath = null;
        boolean hasTheLock = false;
        boolean isDone = false;
        int retryCount = 0;

        //网络闪断需要重试一试
        while (!isDone) {
            isDone = true;

            try {
                // 在/locker下创建临时的顺序节点
                ourPath = createLockNode(client, path);
                System.out.println("已创建临时节点，开始判断是否获取了锁===="+ourPath);
                // 判断你自己是否获得了锁，如果没获得那么我们等待直到获取锁或者超时
                hasTheLock = waitToLock(startMillis, millisToWait, ourPath);
            } catch (ZkNoNodeException e) {
                if (retryCount++ < MAX_RETRY_COUNT) {
                    isDone = false;
                } else {
                    throw e;
                }
            }
        }

        if (hasTheLock) {
            return ourPath;
        }

        return null;
    }

    private void deleteOurPath(String ourPath) throws Exception {
        client.delete(ourPath);
    }

    private String createLockNode(ZkClient client, String path) throws Exception {
        // 创建临时循序节点
        return client.createEphemeralSequential(path, null);
    }
    
    public abstract boolean isGetTheLock(List<String> children, String ourPath) ;
    
    public abstract String getWatchPath(List<String> children, String ourPath);
    
    protected int getOurIndex(List<String> children, String ourPath){
	    // 获取顺序节点的名字 如:/locker/lock-0000000013 > lock-0000000013
        String sequenceNodeName = ourPath.substring(basePath.length() + 1);

        // 获取该节点在所有有序子节点位置
        int ourIndex = children.indexOf(sequenceNodeName);
        if (ourIndex < 0) {
            // 可能网络闪断 抛给上层处理
            throw new ZkNoNodeException("节点没有找到: " + sequenceNodeName);
        }
        
        return ourIndex;
	}

}
