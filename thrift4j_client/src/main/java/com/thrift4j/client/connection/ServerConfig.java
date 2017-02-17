package com.thrift4j.client.connection;

import java.util.List;

import com.thrift4j.client.route.Node;

import lombok.Data;

@Data
public class ServerConfig {
	//服务名
	private String serviceName;
	//服务中暴露多个接口名称
	private List<Node> nodeList;
	
	private String methodName;
	
	
	
	
}