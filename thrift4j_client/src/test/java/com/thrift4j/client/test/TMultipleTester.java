package com.thrift4j.client.test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.thrift4j.client.connection.IfacePoolFactory;
import com.thrift4j.client.connection.ServerConfig;
import com.thrift4j.client.connection.TMultiplexedProtocolBuilder;
import com.thrift4j.client.route.Node;
import com.thrift4j.client.util.IFaceQuene;
import com.thrift4j.client.util.TQuene;

import cmccss.contract.User;
import cmccss.contract.UsesrService;
import cmccss.contract.UsesrService.Client;
import junit.framework.TestCase;

public class TMultipleTester extends TestCase{
	
	
	public void testGenPool() throws Exception{
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setMinIdle(1);
		poolConfig.setMaxTotal(10);
		poolConfig.setTestWhileIdle(true);
		//poolConfig.setTestOnBorrow(true);
		poolConfig.setMinEvictableIdleTimeMillis(60000);
		poolConfig.setTimeBetweenEvictionRunsMillis(30000);
		poolConfig.setNumTestsPerEvictionRun(-1);
		Node node = new Node();
		node.setIp("10.0.3.77");
		node.setPort(8091);
		node.setTimeout(100);
		node.setClazz(UsesrService.Client.class);
		IfacePoolFactory ifacePoolFactory = new IfacePoolFactory(poolConfig, node);
		AtomicInteger total = new AtomicInteger();
		while(true){
			UsesrService.Client testClient = (UsesrService.Client) ifacePoolFactory.getConnection();
			User user  = new User();
		    user.setDicts("abc");
		    user.setName("name");
		    user.setId(1101);
		    
		    node.setClazz(UsesrService.Client.class);
		    try{
		    	 testClient.recommend(user);
		    }catch(Exception e){
		    	e.printStackTrace();
		    }finally{
		    	ifacePoolFactory.releaseConnection(testClient);
		    }
		    int totalCount = total.incrementAndGet();
		    if(totalCount%1000 == 0){
		    	System.out.println(totalCount);
		    }
		}
		
	   
	}
	
	public void testIface() throws TTransportException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		ServerConfig serverConfig = new ServerConfig();
		serverConfig.setServiceName("ucenter");
		Node node = new Node();
		node.setIp("127.0.0.1");
		node.setPort(8091);
		node.setTimeout(100);
		node.setClazz(UsesrService.Client.class);
		List<Node> nodeList = new ArrayList<Node>();
		nodeList.add(node);
		serverConfig.setNodeList(nodeList);
		AtomicInteger total = new AtomicInteger(0);
		IFaceQuene quene =  TMultiplexedProtocolBuilder.buildIface(serverConfig);
		while(true){
			UsesrService.Client testClient = (UsesrService.Client) quene.poll();
			User user  = new User();
		    user.setDicts("abc");
		    user.setName("name");
		    user.setId(1101);
		    try{
		    	 testClient.recommend(user);
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
		    int result = total.incrementAndGet();
		    if(result % 1000 == 0){
		    	System.out.println("invoke result " + result);
		    }
		    quene.enqueue(testClient);
		}
	}
	
	public void testMultiple() throws TTransportException, InterruptedException{
		ServerConfig serverConfig = new ServerConfig();
		serverConfig.setServiceName("ucenter");
		Node node = new Node();
		node.setIp("127.0.0.1");
		node.setPort(8091);
		node.setTimeout(100);
		List<Node> nodeList = new ArrayList<Node>();
		nodeList.add(node);
		serverConfig.setNodeList(nodeList);
		TQuene tqune = TMultiplexedProtocolBuilder.build(serverConfig);
		AtomicInteger total = new AtomicInteger();
		TSocket tsocket = tqune.poll();
		tsocket.open();
		TTransport transport = tsocket;
	    transport = new TFramedTransport(transport);
	    TProtocol tProtocol = new TCompactProtocol(transport);
	    
	    TMultiplexedProtocol cmccProocol = new  TMultiplexedProtocol(tProtocol,"cmccss.contract.UsesrService.Iface");
	    UsesrService.Client testClient =
		  	      new UsesrService.Client(cmccProocol);
		while(true){
		    User user  = new User();
		    user.setDicts("abc");
		    user.setName("name");
		    user.setId(1101);
		    try{
		    	 testClient.recommend(user);
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
		    
		    
		    int result = total.incrementAndGet();
		    if(result % 1000 == 0){
		    	System.out.println("invoke result " + result);
		    }
		    tqune.put(tsocket);
		}
	}
}
