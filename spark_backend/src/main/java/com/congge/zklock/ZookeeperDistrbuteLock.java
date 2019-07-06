package com.congge.zklock;

import java.util.concurrent.CountDownLatch;

import org.I0Itec.zkclient.IZkDataListener;

public class ZookeeperDistrbuteLock extends ZookeeperAbstractLock {

	@Override
	boolean tryLock() {
		try {
			zkClient.createEphemeral(lockPath);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * �ȴ�������ʵ����ע��ڵ�ļ����¼������������ĳ���ڵ��״̬�����˱仯���ͼ�����ȡ��
	 */
	@Override
	void waitLock() {

		IZkDataListener listener = new IZkDataListener() {

			public void handleDataDeleted(String path) throws Exception {
				if (countDownLatch != null) {
					countDownLatch.countDown();
				}
			}

			public void handleDataChange(String arg0, Object arg1) throws Exception {

			}
		};

		// ע���¼�֪ͨ
		zkClient.subscribeDataChanges(lockPath, listener);

		// ����ڵ��Ѿ�������
		if (zkClient.exists(lockPath)) {
			countDownLatch = new CountDownLatch(1);
			try {
				countDownLatch.await();
			} catch (Exception e) {

			}
		}

		// ������Ϻ��Ƴ��¼�֪ͨ
		zkClient.unsubscribeDataChanges(lockPath, listener);

	}

}
