package org.amie.exercise.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class ZKForClient{
	
	ZooKeeper zk = null;
	
	String address;
	CountDownLatch latch = new CountDownLatch(1);
	volatile List<String> servers = null;
	static final String ZK_SERVER_ROOT = "/servers";
	
	public ZKForClient(String address){
		this.address = address;
		
		try {
			connectZK();
			latch.await();
			getChildren();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void connectZK() throws Exception {
		    zk = new ZooKeeper(address,2000, new Watcher(){
			public void process(WatchedEvent event) {
			 if((latch.getCount()>0) && event.getState().equals(Event.KeeperState.SyncConnected)){
				 latch.countDown();		
				 System.out.println("count down");
			 }				
			}
		});
	}
	
	private void getChildren() throws Exception {
		System.out.println("get Children");
		List<String> childrenPaths = zk.getChildren(ZK_SERVER_ROOT, new Watcher(){
			public void process(WatchedEvent event) {
				try {
					System.out.println("in get children's watcher's process "+ event.getType());
					getChildren();
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}			
		});
		List<String> tmpServers = new ArrayList<String>();
		for(String p :childrenPaths){
			System.out.println(p);
			String server = new String(zk.getData(ZK_SERVER_ROOT+ "/"+p, false, null));
			tmpServers.add(server);
		}
		this.servers = tmpServers;
		
		
	}

	public List<String> getServers() {
		return this.servers;
		
	}
	public String getOneServer() {
		if(servers!=null&& servers.size()>0){
			return servers.get(0);
		}
		return null;
		
	}

}
