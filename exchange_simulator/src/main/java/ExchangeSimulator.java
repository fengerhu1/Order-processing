import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;
import rwlock.WriteLock;
import zk.ZkSerialize;
import zk.ZookeeperData;

import java.util.Random;

/**
 * @author Allen
 * @date 2019/7/8 20:34
 */
public class ExchangeSimulator implements Runnable{
    private static final int CURRENCY_NUM = 4;
    private static String[] currency_list = {"RMB", "JPY", "EUR", "USD"};
    private static double[] exchange_rate_list = {2.0, 0.15, 9.0, 12.0};

    private String currency;
    private double rate;
    private double max_rate;
    private double min_rate;

    public static void main(String[] args) {
        Thread[] simulator_threads = new Thread[CURRENCY_NUM];
        for(int i = 0; i < CURRENCY_NUM; i++){
            simulator_threads[i] = new Thread(
                    new ExchangeSimulator(currency_list[i], exchange_rate_list[i]));
            simulator_threads[i].start();
        }
        Thread.yield();
    }

    public ExchangeSimulator(String currency, double exchange_rate){
        this.currency = currency;
        this.max_rate = exchange_rate*1.2;
        this.rate = exchange_rate;
        this.min_rate = exchange_rate*0.8;
    }

    public void run() {
        while(true) {
            try {
                Thread.sleep(1000 * 60);
            }catch (Exception e){
                e.printStackTrace();
            }
            modifyExchangeRate();
        }
    }

    private void modifyExchangeRate(){
        ZkClient zkClientForLock = new ZkClient("10.0.0.77:2181,10.0.0.154:2181,10.0.0.137:2181,10.0.0.115:2181",
                5000, 5000, new BytesPushThroughSerializer());
        ZkClient zkClientForData = new ZkClient("10.0.0.77:2181,10.0.0.154:2181,10.0.0.137:2181,10.0.0.115:2181",
                5000, 5000, new ZkSerialize());
        WriteLock exchange_rate_lock = new WriteLock(zkClientForLock, '/'+currency);
        try {
            exchange_rate_lock.getLock();
        }catch (Exception e){
            System.out.println(e.getMessage());
            return;
        }
        double order_currency =  Float.parseFloat(ZookeeperData.returndata(zkClientForData,"/currency/"+currency,new ZkSerialize()));
        Random random=new Random(System.currentTimeMillis());
        //最多涨跌两个百分点
        double change = random.nextDouble()*0.02*rate;
        if(random.nextBoolean()){
            order_currency += change;
        }else{
            order_currency -= change;
        }
        //不能超过限制
        if(order_currency > max_rate) order_currency = max_rate;
        if(order_currency < min_rate) order_currency = min_rate;
        zkClientForData.writeData("/currency/"+currency, String.valueOf(order_currency));
        try {
            exchange_rate_lock.releaseLock();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
