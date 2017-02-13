package com.thrift4j.server.config;

import org.apache.thrift.TProcessor;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class ServiceProviderConfig {
	private String name;
	private TProcessor processor;
}
