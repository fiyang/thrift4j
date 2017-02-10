package com.thrift4j.etcd.configure;

import java.net.URI;
import java.util.List;

import lombok.Data;
@Data
public class EtcdClientProperties {
		
	  List<URI> uris;
	  
	  int retryTimes = 3;
	  
	  int beforeRetryTime = 200;
	  
}
