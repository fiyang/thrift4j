package com.thrift4j.client.config;




import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.thrift.transport.TTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thrift4j.client.connection.TransportPoolFactory;
import com.thrift4j.client.route.Node;


@Configuration
public class ThriftClientConfiguration {


  @Bean(destroyMethod = "close")
  public GenericKeyedObjectPool<Node, TTransport> thriftClientsPool(ThriftClientProperties thriftClientProperties) {
    GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
    config.setJmxEnabled(false); //cause spring will autodetect itself
    config.setMaxTotalPerKey(thriftClientProperties.getPoolMaxTotalPerKey());
    config.setMaxIdlePerKey(thriftClientProperties.getPoolMaxIdlePerKey());
    config.setMinIdlePerKey(thriftClientProperties.getPoolMinIdlePerKey());
    config.setMaxWaitMillis(thriftClientProperties.getPoolMaxWait());
    // TODO fixbug设置每次还回对象的时候，进行对象的正确性判断
    config.setTestOnReturn(true);
    TransportPoolFactory tpf = new TransportPoolFactory();
    return new GenericKeyedObjectPool<>(tpf, config);
  }
}
