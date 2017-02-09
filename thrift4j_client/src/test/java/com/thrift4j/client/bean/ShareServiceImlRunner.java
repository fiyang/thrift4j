package com.thrift4j.client.bean;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.thrift4j.client.annotation.ThriftClient;

@Component
public class ShareServiceImlRunner {
	@ThriftClient
	public SharedService.Iface client;
	
	public void doSomeThing() throws TException{
		client.getStruct(10);
	}
}
