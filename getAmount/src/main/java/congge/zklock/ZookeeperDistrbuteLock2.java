package congge.zklock;

import org.I0Itec.zkclient.IZkDataListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZookeeperDistrbuteLock2 extends ZookeeperAbstractLock{

    private CountDownLatch countDownLatch = null;
    private String nodepath;
    private String beforePath;		//前一个节点
    private String currentPath;		//当前节点

    //初始化主节点，如果不存在则创建
    public ZookeeperDistrbuteLock2(){
        if(!this.zkClient.exists(this.nodepath)){
            this.zkClient.createPersistent(this.nodepath);
        }
    }
    public ZookeeperDistrbuteLock2(String nodepath)
    {
        this.nodepath = nodepath;
        System.out.println(this.nodepath);
        if(!this.zkClient.exists(this.nodepath)){
            this.zkClient.createPersistent(this.nodepath);
        }
    }
    @Override
    public boolean tryLock() {
        //基于lock2节点，新建一个临时节点
        if(currentPath == null || currentPath.length() <= 0){
            currentPath = this.zkClient.createEphemeralSequential(this.nodepath + "/", beforePath);
        }
        //获取所有临时节点并进行排序
        List<String> children = this.zkClient.getChildren(this.nodepath);
        Collections.sort(children);

        if(currentPath.equals(this.nodepath + "/" + children.get(0))){
            return true;
        }else{
            //如果当前节点在节点列表中不是排第一的位置，则获取当前节点前面的节点，并赋值
            int wz = Collections.binarySearch(children, currentPath.substring(7));
            beforePath = this.nodepath + "/" + children.get(wz-1);
        }
        return false;
    }

    @Override
    public void waitLock() {	//等待锁
        IZkDataListener iZkDataListener = new IZkDataListener() {

            public void handleDataDeleted(String dataPath) throws Exception {
                //唤醒被等待的线程
                if(countDownLatch != null){
                    countDownLatch.countDown();
                }
            }
            public void handleDataChange(String path, Object data) throws Exception {

            }
        };
        //注册事件，对前一个节点进行监听
        zkClient.subscribeDataChanges(beforePath, iZkDataListener);

        //如果节点存在了，则需要等待一直到接收到事件通知
        if(zkClient.exists(beforePath)){
            countDownLatch = new CountDownLatch(1);
            try {
                countDownLatch.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        zkClient.unsubscribeDataChanges(beforePath, iZkDataListener);

    }

    //释放锁
    public void unlock() {
        zkClient.delete(currentPath);
        zkClient.close();
    }

}
