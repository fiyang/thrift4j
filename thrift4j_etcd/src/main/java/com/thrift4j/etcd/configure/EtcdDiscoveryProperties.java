package com.thrift4j.etcd.configure;


import lombok.Data;

@Data
public class EtcdDiscoveryProperties {
  int heartbeat = 5000;
  int ttl = 10;

}
