package com.congge.zklock;

/**
 * 定义锁接口
 * 
 * @author asus
 *
 */
public interface ExtLock {
	// 获取锁
	public void getLock();

	// 释放锁
	public void unLock();
}
