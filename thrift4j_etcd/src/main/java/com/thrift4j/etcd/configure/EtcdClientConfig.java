package com.thrift4j.etcd.configure;


import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import lombok.Data;


@Data
public class EtcdClientConfig {
	
  List<URI> uris;
  
  int retryTimes = 3;
  
  int beforeRetryTime = 200;
  
}
