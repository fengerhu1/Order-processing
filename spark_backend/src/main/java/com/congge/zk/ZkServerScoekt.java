package com.congge.zk;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.I0Itec.zkclient.ZkClient;


/**
 * ServerScoekt服务端
 * @author asus
 *
 */
public class ZkServerScoekt implements Runnable {
	
	//这里是写死的端口，实际应用中，是提前规划好，然后通过配置文件或者全局的常量池进行读取
	private static int port = 18081;
	
	//定义父节点，同上，最好通过配置文件进行读取和管理
	private static String parentPath = "/congge_service";

	public static void main(String[] args) throws IOException {

		ZkServerScoekt server = new ZkServerScoekt(port);
		Thread thread = new Thread(server);
		thread.start();
	}

	public ZkServerScoekt(int port) {
		this.port = port;
	}

	/**
	 * 初始服务端连接信息，并创建注册到zookeeper上面的相关节点信息
	 */
	private void registServer() {
		
		//1、建立zk连接
		ZkClient zkClient = new ZkClient("127.0.0.1:2181", 6000, 1000);
		
		//2、先创建父节点
		if(!zkClient.exists(parentPath)){
			zkClient.createPersistent(parentPath);
		}
		
		//3、创建子节点
		String nodeName = parentPath + "/service_" + port;
		String nodeValue = "127.0.0.1:"+port;
		
		if(zkClient.exists(nodeName)){
			zkClient.delete(nodeName);
		}
		
		//创建临时子节点，用于服务发现
		zkClient.createEphemeral(nodeName,nodeValue);
		System.out.println("服务节点注册成功");
		
	}

	public void run() {
		
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			
			registServer();
			
			System.out.println("Server start port:" + port);
			
			Socket socket = null;
			while (true) {
				socket = serverSocket.accept();
				new Thread(new ServerHandler(socket)).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (serverSocket != null) {
					serverSocket.close();
				}
			} catch (Exception e2) {

			}
		}
	}

}