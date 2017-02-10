package com.thrift4j.etcd.configure;




import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import mousio.client.retry.RetryNTimes;
import mousio.etcd4j.EtcdClient;


@Configuration
@Slf4j
public class EtcdAutoConfiguration {


  @Bean
  public EtcdClient etcdClient(EtcdClientProperties etcdClientProperties) {
    List<URI> uriList = etcdClientProperties.getUris();
    if (uriList == null || uriList.isEmpty()) {
      log.error("uri has not been set");
      return null;
    }

    EtcdClient client = new EtcdClient(uriList.toArray(new URI[uriList.size()]));
    client.setRetryHandler(new RetryNTimes(etcdClientProperties.getBeforeRetryTime(),
        etcdClientProperties.getRetryTimes()));

    if (client.version() == null) {
      log.info("etcd urls are [ {} ] is invalid",
          etcdClientProperties.getUris().stream().map(uri -> uri.toString())
              .collect(Collectors.joining(", ")));
    } else {
      log.info("etcd version is {} , urls are [ {} ]", client.version().getCluster(),
          etcdClientProperties.getUris().stream().map(uri -> uri.toString())
              .collect(Collectors.joining(", ")));
    }
    return client;
  }
}
