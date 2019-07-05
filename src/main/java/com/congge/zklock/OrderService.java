/**
 * 功能说明:
 * 功能作者:
 * 创建日期:
 * 版权归属:每特教育|蚂蚁课堂所有 www.itmayiedu.com
 */
package com.congge.zklock;


public class OrderService implements Runnable {
	
	private OrderNumGenerator orderNumGenerator = new OrderNumGenerator();
	
	//private ExtLock extLock = new ZookeeperDistrbuteLock();
	private ExtLock extLock = new ZookeeperDistrbuteLock2("/lock2");
	public void run() {
		getNumber();
	}

	public void getNumber() {
		try {
			extLock.getLock();
			System.out.println("getNumber");
			String number = orderNumGenerator.getNumber();
			System.out.println("thread:" + Thread.currentThread().getName() + ",orderId:" + number);
		} catch (Exception e) {

		} finally {
			extLock.unLock();
		}
	}

	public static void main(String[] args) {
		System.out.println("multi thread number");
		// OrderService orderService = new OrderService();
		for (int i = 0; i < 100; i++) {
			new Thread(new OrderService()).start();
		}
	}

}
