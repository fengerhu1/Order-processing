package src;

import org.I0Itec.zkclient.ZkClient;

public class ZkCreate {

    static private ZkClient zkClient4 = new ZkClient("10.0.0.77:2181,10.0.0.154:2181,10.0.0.137:2181,10.0.0.115:2181");
    static public ZkClient getZK()
    {
        return zkClient4;
    }
}
