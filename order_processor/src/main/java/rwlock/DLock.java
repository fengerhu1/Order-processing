//package rwlock;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//import org.I0Itec.zkclient.IZkDataListener;
//import org.I0Itec.zkclient.ZkClient;
//import org.I0Itec.zkclient.exception.ZkNoNodeException;
//public final class DLock
//{
//    private ZkClient zkClient;
//    private String lockName;
//    private String thisReadLock;
//    private String thisWriteLock;
//
//    /**
//     * 分布式锁连接zookeeper 以及 初始化锁主节点
//     *
//     * @param hostUrl  zookeeper 连接url
//     * @param lockName 锁主节点
//     */
//    public void connect(String hostUrl , String lockName)
//    {
//        this.lockName = lockName;
//        zkClient = new ZkClient(hostUrl);
//        if (!zkClient.exists(lockName))
//            zkClient.createPersistent(lockName);
//    }
//
//    /**
//     * 获取读锁
//     */
//    public void lockRead()
//    {
//        CountDownLatch readLatch = new CountDownLatch(1);
//        // 创建此临时节点， 获取带有顺序号的完整节点
//        String thisLockNodeBuilder = lockName +
//                "/" +
//                LockType.READ +
//                "-";
//        thisReadLock = zkClient.createEphemeralSequential(thisLockNodeBuilder , "");
//
//        // 找到此读锁前一个写锁
//        List<String> tmp_nodes = zkClient.getChildren(lockName);
//        sortNodes(tmp_nodes);
//        tmp_nodes.forEach(System.out::println);
//        int tmp_index = 0;
//        for (int i = tmp_nodes.size() - 1; i >= 0; i--)
//        {
//            if (thisReadLock.equals(lockName + "/" + tmp_nodes.get(i)))
//            {
//                tmp_index = i;
//            } else if (i < tmp_index && tmp_nodes.get(i).split("-")[0].equals(LockType.WRITE.toString()))
//            {
//                // 找到当前读锁之前的一个写锁
//                // 先监听此写锁，再阻塞当前读锁
//                zkClient.subscribeChildChanges(lockName + "/" + tmp_nodes.get(i) , (parentPath , currentChilds) -> readLatch.countDown());
//
//                try
//                {
//                    readLatch.await();
//                } catch (InterruptedException e)
//                {
//                    e.printStackTrace();
//                }
//                break;
//            }
//        }
//    }
//
//    /**
//     * 释放读锁
//     */
//    public void unLockRead()
//    {
//        if (this.thisReadLock != null)
//        {
//            zkClient.delete(thisReadLock);
//            thisReadLock = null;
//        }
//    }
//
//    /**
//     * 获取写锁
//     */
//    public void lockWrite()
//    {
//        CountDownLatch writeLatch = new CountDownLatch(1);
//        // 创建此临时节点， 获取带有顺序号的完整节点
//        String thisLockNodeBuilder = lockName +
//                "/" +
//                LockType.WRITE +
//                "-";
//        thisWriteLock = zkClient.createEphemeralSequential(thisLockNodeBuilder , "");
//
//        List<String> tmp_nodes = zkClient.getChildren(lockName);
//        sortNodes(tmp_nodes);
//        for (int i = tmp_nodes.size() - 1; i >= 0; i--)
//        {
//            if (thisWriteLock.equals(lockName + "/" + tmp_nodes.get(i)))
//            {
//                // 在锁列表中找到此写锁
//
//                if (i > 0)
//                {
//                    // 如果此写锁前面还有锁
//                    // 监听前面的锁， 然后阻塞当前写锁获取
//
//                    zkClient.subscribeChildChanges(lockName + "/" + tmp_nodes.get(i - 1) , (parentPath , currentChilds) -> writeLatch.countDown());
//
//                    try
//                    {
//                        // 阻塞当前写锁获取
//                        writeLatch.await();
//                    } catch (InterruptedException e)
//                    {
//                        e.printStackTrace();
//                    }
//                    break;
//                }
//            }
//        }
//    }
//
//    /**
//     * 释放写锁
//     */
//    public void unLockWrite()
//    {
//        if (thisWriteLock != null)
//        {
//            zkClient.delete(thisWriteLock);
//            thisWriteLock = null;
//        }
//    }
//
//    /**
//     * 节点按照顺序号排序
//     *
//     * @param nodes 临时节点
//     */
//    private void sortNodes(List<String> nodes)
//    {
//        nodes.sort(Comparator.comparing(o -> o.split("-")[1]));
//    }
//
//    /**
//     * 锁类型枚举
//     */
//    private enum LockType
//    {
//        READ,
//        WRITE;
//    }
//}
//
