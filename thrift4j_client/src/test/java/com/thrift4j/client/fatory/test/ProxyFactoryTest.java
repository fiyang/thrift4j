package com.thrift4j.client.fatory.test;


import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.thrift4j.client.bean.ShareServiceImlRunner;
import com.thrift4j.client.bean.SharedService;
import com.thrift4j.client.bean.SharedStruct;
import com.thrift4j.client.route.Node;

import junit.framework.TestCase;

public class ProxyFactoryTest extends TestCase{
	
	
	 public static final String DEFAULT_SPRING_CONFIG = "classpath*:META-INF/spring/*.xml";

	 static ClassPathXmlApplicationContext context;
	
	public void testInit() throws TException{
		context = new ClassPathXmlApplicationContext(DEFAULT_SPRING_CONFIG);
		GenericKeyedObjectPool pool = context.getBean("thriftClientsPool", GenericKeyedObjectPool.class);
		assertNotNull(pool);
		
		//ShareServiceImlRunner sareServiceImpl = (ShareServiceImlRunner) context.getBean("ShareServiceImlRunner");
		//sareServiceImpl.doSomeThing();
	}
	
	public void testBean(){
		SharedStruct shareStruct = new SharedStruct();
		shareStruct.setValue("name");
		shareStruct.setKey(1001);
		
		SharedService.Client client;
	}
	
}
