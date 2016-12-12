package org.amie.exercise.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.amie.exercise.annotation.RPCService;
import org.amie.exercise.bean.Request;
import org.amie.exercise.bean.Response;
import org.amie.exercise.handler.DecodeFromByteBufHandler;
import org.amie.exercise.handler.EncodeToByteBufHandler;
import org.amie.exercise.handler.RPCServerHandler;
import org.apache.zookeeper.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class RpcServerBootstrap implements ApplicationContextAware,InitializingBean{
	String nettyServerAddress;
	String zkServerAdress;
	Map rpcServices;
	CountDownLatch latch = new CountDownLatch(1);
	static final String ZK_SERVER_ROOT = "/servers";

	
	public RpcServerBootstrap(String nettyServerAddress, String zkServerAdress) {		
		super();
		this.nettyServerAddress = nettyServerAddress;
		this.zkServerAdress = zkServerAdress;
		rpcServices = new HashMap<String, Object>();
		
		System.out.println("in RpcServerBootstrap constructor, nettyServerAddress "
		+ nettyServerAddress + ", zkServerAdress "+ zkServerAdress);
	}

	

	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		System.out.println("in set applicationContext");
		Map<String, Object> beans = ctx.getBeansWithAnnotation(RPCService.class);
		if(beans==null){
			throw new RuntimeException("No RPCService found");
		}
		for(Object bean : beans.values()){
			Class<?> parentClass = (bean.getClass().getAnnotation(RPCService.class)).parentClass();
			rpcServices.put(parentClass.getName(), bean);
		}
		System.out.println("load complete for all RPC services : "+ rpcServices.size());
	}

	public void afterPropertiesSet() throws Exception {
		/*
		 * 虽然用户将此类写到了spring的xml 里,spring 会负责构建这个bean. 但是如果在bean的constructor 里就把netty 起起来,
		 *  很容易出问题,因为那会也许有的bean还没有load 完. 要想让所有bean/properties/xml 都初始化完毕后, 再 起netty server
		 *  最保险的办法就是让此bean 继承ApplicationContextAware/InitializingBean, 让spring 在初始化结束后, 回调此类里的此方法.
		 */
		ServerBootstrap bootstrap = new ServerBootstrap();
		EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
		bootstrap.group(eventLoopGroup)
		.channel(NioServerSocketChannel.class)
		.childHandler(new ChannelInitializer<SocketChannel>(){

			@Override
			protected void initChannel(SocketChannel socketChannel) throws Exception {
				socketChannel.pipeline().addLast(new DecodeFromByteBufHandler(Request.class));
				socketChannel.pipeline().addLast(new EncodeToByteBufHandler(Response.class));
				socketChannel.pipeline().addLast(new RPCServerHandler(rpcServices));
				
			}
			
		});

		String[] array = nettyServerAddress.split(":");
		String host = array[0];
		int port = Integer.parseInt(array[1]);

		ChannelFuture channelFuture = bootstrap.bind(host,port).sync();
		System.out.println("server started on " + host + ":" + port);



		ZooKeeper zk = new ZooKeeper(zkServerAdress, 2000, new Watcher() {
			public void process(WatchedEvent watchedEvent) {
				if(latch.getCount()>0 && Event.KeeperState.SyncConnected.equals(watchedEvent.getState())){
					System.out.println("zk count down");
					latch.countDown();
				}

			}
		});
		latch.await();
		if(zk.exists(ZK_SERVER_ROOT,false)==null){
			zk.create(ZK_SERVER_ROOT,null, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
		}
		zk.create(ZK_SERVER_ROOT+"/server",(host+":"+port).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

	}

}
