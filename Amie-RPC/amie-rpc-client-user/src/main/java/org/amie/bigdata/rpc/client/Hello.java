package org.amie.bigdata.rpc.client;

import org.amie.exercise.bean.Response;
import org.amie.exercise.client.RPCProxy;
import org.amie.exercise.service.Processor;
import org.springframework.context.support.ClassPathXmlApplicationContext;
public class Hello {

	public static void main(String[] args){
		ClassPathXmlApplicationContext contex = new ClassPathXmlApplicationContext(new String[]{
				"classpath:spring-client.xml"
		});
		RPCProxy rpcProxy = (RPCProxy) contex.getBean("rpcProxy");
		Processor p = (Processor) rpcProxy.createProxy(Processor.class);
		String r = p.hello("amie");
		System.out.println("result : "+ r);
	}

}
