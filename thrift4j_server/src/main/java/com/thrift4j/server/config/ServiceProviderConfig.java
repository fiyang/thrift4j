package com.thrift4j.server.config;

import org.apache.thrift.TProcessor;

import lombok.Data;

@Data
public class ServiceProviderConfig {
	private String name;
	private TProcessor processor;
}
