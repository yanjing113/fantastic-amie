package org.amie.exercise.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.amie.exercise.bean.Request;
import org.amie.exercise.bean.Response;
import org.amie.exercise.handler.DecodeFromByteBufHandler;
import org.amie.exercise.handler.EncodeToByteBufHandler;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.util.StringUtils;

public class RPCProxy {	 
	 CountDownLatch latch = new CountDownLatch(1);
	 ZKForClient zkForClient;
	 
	 public RPCProxy(ZKForClient zkForClient){
		this.zkForClient = zkForClient;
	 }
	 
	public  Object createProxy(final Class<?> Interface){
		return Proxy.newProxyInstance(Interface.getClassLoader(), new Class<?>[]{Interface}, new InvocationHandler(){

			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				Response  response = null;
				Request r = new Request();
				r.setRequestId(UUID.randomUUID().toString());
				r.setParentClassName(Interface.getName());
				r.setMethodName(method.getName());
				r.setParameters(args);
				r.setParameterTypes(method.getParameterTypes());
				System.out.println("request "+ r);
				String server = zkForClient.getOneServer();
				if(StringUtils.hasText(server)){
					System.out.println(server);
					//get netty server address
					String[] fields = server.split(":");
					String host = fields[0];
					int port = Integer.parseInt(fields[1]);
					
					//starting netty client
					Bootstrap bootstrap = new Bootstrap();
					EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
					final ClientHandler clientHandler = new ClientHandler();
					try {
						bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
						.handler(new ChannelInitializer<SocketChannel>(){

							@Override
							protected void initChannel(SocketChannel channel)
									throws Exception {
								channel.pipeline().addLast(new DecodeFromByteBufHandler(Response.class));							
								channel.pipeline().addLast(new EncodeToByteBufHandler(Request.class));
								channel.pipeline().addLast(clientHandler);
								
							}
							
						});
						ChannelFuture channelFuture1 = bootstrap.connect(host, port).sync();
						System.out.println("connected to the server");
						ChannelFuture channelFuture2 = channelFuture1.channel().writeAndFlush(r).sync();
						System.out.println("channelFuture1==channelFuture2? "+ (channelFuture1==channelFuture2));
						channelFuture1.channel().closeFuture().sync();
						
                   //	ChannelFuture sync = future.channel().closeFuture().sync();
						response = clientHandler.getResponse();
						System.out.println("response "+ response);
						
						if (response!=null && response.getException()!=null) {
							throw response.getException();
						} else if (response!=null){
							return response.getResult();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						System.out.println("in finally");
						eventLoopGroup.shutdownGracefully();
					}
				}
				
				return "nothing return";
			}
			
		});
		
	}
}
