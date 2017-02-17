package com.thrift4j.server.bootstrap;

import java.util.concurrent.CountDownLatch;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.thrift4j.etcd.register.EtcdRegister;

public class Main {
	public static final String DEFAULT_SPRING_CONFIG = "classpath*:/META-INF/spring/*.xml";

	static ClassPathXmlApplicationContext context;
	
	public static void main(String [] args) throws Exception{
		context = new ClassPathXmlApplicationContext(DEFAULT_SPRING_CONFIG);
		EtcdRegister register = (EtcdRegister)context.getBean("etcdRegister");
		System.out.println(register.getKey());
		System.out.println(register.getPath());
		System.out.println(register.getValue());
		NBServer nbServer = new NBServer(GlobalContext.getInstance().getProviderList(), 8091);
		nbServer.startServer();
		CountDownLatch latch = new CountDownLatch(1);
		latch.await();
	}
}
