package src;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 方法超过执行时间后抛出异常
 * @author Gary Huang
 * create Time；2014-12-2
 * email: 834865081@qq.com
 * Copyright:归个人所有，转载请表名 出处
 * 个人博客地址：http://blog.csdn.net/hfmbook
 * */
public class ObtainLease {

    /***
     * 方法参数说明
     * @param target 调用方法的当前对象
     * @param methodName 方法名称
     * @param parameterTypes 调用方法的参数类型
     * @param params 参数  可以传递多个参数
     *
     * */
    private Transaction tx;
    public  Object callMethod(final Object target , final String methodName ,final Class<?>[] parameterTypes,final Object[]params){
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        FutureTask<String> future = new FutureTask<String>(new Callable<String>() {
            public String call() throws Exception {
                String value = null  ;
                try {
                    Method method = null ;
                    method = target.getClass().getDeclaredMethod(methodName , parameterTypes ) ;

                    Object returnValue = method.invoke(target, params) ;
                    value = returnValue != null ? returnValue.toString() : null ;
                } catch (Exception e) {
                    e.printStackTrace() ;
                    throw e ;
                }
                return value ;
            }
        });

        executorService.execute(future);
        String result = null;
        try{
            /**获取方法返回值 并设定方法执行的时间为10秒*/
            result = future.get(10 , TimeUnit.SECONDS );

        }catch (InterruptedException e) {
            future.cancel(true);
            System.out.println("订单处理中断");
            this.tx.rollback();
        }catch (ExecutionException e) {
            future.cancel(true);
            System.out.println("订单处理报异常");
            this.tx.rollback();
        }catch (TimeoutException e) {
            future.cancel(true);
            this.tx.rollback();
            throw new RuntimeException("lease timeout" , e );

        }
        executorService.shutdownNow();
        return result ;
    }

    public Object call(Integer id){
        try {
            Thread.sleep( 11000 ) ;
        } catch (Exception e) {
        }
        return id ;
    }

    public ObtainLease(Transaction tx)
    {
        this.tx = tx;
    }
    public  void main() {
        System.out.println( callMethod(new ObtainLease(this.tx), "call" , new Class<?>[]{Integer.class}, new Object[]{ 1523 } ) ) ;
    }
}