package com.thrift4j.client.config;


import java.lang.reflect.Constructor;

import com.thrift4j.client.route.Router;

import lombok.Data;

@Data
public class ThriftClientConfig {
	private String address;
	
	private int timeout;
	
	private int retryTimes;

	private Router router;
	
	private Constructor<?> clientConstructor;
}
