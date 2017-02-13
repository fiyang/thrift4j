package com.thrift4j.server.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.thrift4j.etcd.configure.EtcdAutoConfiguration;
import com.thrift4j.etcd.register.EtcdRegister;
import com.thrift4j.server.exception.ThriftServerException;
import com.thrift4j.server.util.InetAddressUtil;

import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import mousio.etcd4j.EtcdClient;


@Configuration
@Import(EtcdAutoConfiguration.class)
@Slf4j
public class ThriftRegisterConfiguration {

  private final Pattern DEFAULT_PACKAGE_PATTERN = Pattern.compile(
      "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");

  @Bean
  public EtcdRegister etcdRegister(EtcdClient etcdClient,
      ThriftServerProperties thriftServerProperties) {
    EtcdRegister register = new EtcdRegister();
    String serviceName = thriftServerProperties.getServiceName();

    int lastComma = serviceName.lastIndexOf(".");
    String interfaceName = serviceName.substring(0, lastComma);
    if (!DEFAULT_PACKAGE_PATTERN.matcher(interfaceName).matches()) {
      throw new ThriftServerException("interface name is not match to package pattern");
    }

    register.setPath("/thrift4j/service/" + interfaceName);

    String ip = InetAddressUtil.getLocalHostLANAddress().getHostAddress();

    String address = ip + ":" + String.valueOf(thriftServerProperties.getPort());
    register.setKey(address);
    register.setValue(address);

    register.setClient(etcdClient);
    register.setStart(true);

    String path = register.getPath() + "/" + register.getKey();
    log.info("path is {} register success!", path);
    return register;
  }

}
