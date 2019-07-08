package zk;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.util.List;


public class ZookeeperData {

    private static String zkServer = "10.0.0.77:2181,10.0.0.154:2181,10.0.0.137:2181";//zookeeper地址

    public static void main(String[] args) {
        ZkClient zkClient = new ZkClient(zkServer);//创建zookeeper的java客户端连接

        if(!zkClient.exists("/LAN")) {
            zkClient.createPersistent("/LAN");
        }

        zkClient.setZkSerializer(new ZkSerialize());//这里先设置好序列化工具再写入数据
        zkClient.createEphemeral("/LAN/liu", "liu");
        zkClient.createEphemeral("/LAN/zhou");
        zkClient.writeData("/LAN/zhou", "zhou");
        showZkPathData(zkClient, "/LAN", new ZkSerialize());//展示LAN目录下的所有子目录

        zkClient.close();
    }

    /**
     *  遍历展示目录下的所有节点
     * @author LAN
     * @date 2018年12月3日
     * @param zkClient
     * @param root
     */
    public static void showZkPathData(ZkClient zkClient, String root, ZkSerializer serializer) {
        zkClient.setZkSerializer(serializer);
        List<String> children = zkClient.getChildren(root);
        if(children.isEmpty()){
            return;
        }
        for(String s:children){
            String childPath = root.endsWith("/")?(root+s):(root+"/"+s);
            Object data = zkClient.readData(childPath, true);
            if(data!=null) {
                System.err.println(data.getClass());
            }
            System.err.println(childPath+"("+data+")");
            showZkPathData(zkClient, childPath, serializer);
        }
    }
    public static String returndata(ZkClient zkClient, String root, ZkSerializer serializer) {
        zkClient.setZkSerializer(serializer);
        Object data = zkClient.readData(root, true);
        return data.toString();
    }
}

