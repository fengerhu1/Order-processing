package com.congge.zklock;

import java.util.concurrent.CountDownLatch;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

public abstract class ZookeeperAbstractLock implements ExtLock {

	// ��Ⱥ���ӵ�ַ
	protected String CONNECTION = "10.0.0.77:2181";
	// zk�ͻ�������
	protected ZkClient zkClient = new ZkClient(CONNECTION);
	// path·��
	protected String lockPath = "/path";

	protected static  String PATH = "/lock";

	public   String locknode = "/lock1";

	protected CountDownLatch countDownLatch = new CountDownLatch(1);


	// ��ȡ��
	abstract boolean tryLock();

	// �ȴ���
	abstract void waitLock();

	public void getLock() {
		System.out.println("try lock");
		if (tryLock()) {
			System.out.println("get lock");
		} else {
			System.out.println("wait lock");
			waitLock();
			System.out.println("get2 lock");
			getLock();
		}
	}

	public void unLock() {
		if (zkClient != null) {
			System.out.println("release lock");
			zkClient.close();
		}
	}
}
