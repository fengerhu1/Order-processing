package com.congge.zk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

public class ZkServerClient {
	
	public static List<String> listServer = new ArrayList<String>();
	
	public static String parent = "/congge_service";

	public static void main(String[] args) {
		
		initServer();
		ZkServerClient client = new ZkServerClient();
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		
		while (true) {
			String name;
			try {
				name = console.readLine();
				if ("exit".equals(name)) {
					System.exit(0);
				}
				client.send(name);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 连接zookeeper并获取所有的可用的服务URL地址列表
	 */
	public static void initServer() {
		// listServer.add("127.0.0.1:18080");

		final ZkClient zkClient = new ZkClient("127.0.0.1:2181", 6000, 1000);
		
		//获取父节点下的所有子节点数据
		List<String> children = zkClient.getChildren(parent);
		
		//获取子节点，并存放在全局的list集合中
		getChilds(zkClient, children);
		
		// 监听事件，监听子节点的value变化，模拟节点的上下线，就可以通过该回调方法检测到
		zkClient.subscribeChildChanges(parent, new IZkChildListener() {

			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				getChilds(zkClient, currentChilds);
			}
		});
	}

	/**
	 * 获取子节点
	 * @param zkClient
	 * @param currentChilds
	 */
	private static void getChilds(ZkClient zkClient, List<String> currentChilds) {
		
		//System.out.println("有服务器宕机，获取到新的服务器节点信息是:" + listServer.toString());
		listServer.clear();
		
		for (String p : currentChilds) {
			String pathValue = (String) zkClient.readData(parent + "/" + p);
			listServer.add(pathValue);
		}
		
		serverCount = listServer.size();
		System.out.println("从zk读取到最新的信息:" + listServer.toString());

	}

	// 请求次数
	private static int reqestCount = 1;
	
	// 服务数量
	private static int serverCount = 0;

	/**
	 * 模拟负载均衡算法获取可用的服务端地址信息
	 * @return
	 */
	public static String getServer() {
		// 实现负载均衡
		String serverName = listServer.get(reqestCount % serverCount);
		System.out.println("客戶端请求次数是:" + reqestCount+",对应服务器是:" + serverName);
		++reqestCount;
		return serverName;
	}

	public void send(String name) {

		String server = ZkServerClient.getServer();
		String[] cfg = server.split(":");

		Socket socket = null;
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			socket = new Socket(cfg[0], Integer.parseInt(cfg[1]));
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			out.println(name);
			while (true) {
				String resp = in.readLine();
				if (resp == null)
					break;
				else if (resp.length() > 0) {
					System.out.println("Receive : " + resp);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}