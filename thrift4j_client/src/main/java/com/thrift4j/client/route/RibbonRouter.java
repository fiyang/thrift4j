package com.thrift4j.client.route;



import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.loadbalancer.AvailabilityFilteringRule;
import com.netflix.loadbalancer.DummyPing;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAffinityServerListFilter;
import com.thrift4j.client.config.ThriftServer;
import com.thrift4j.client.config.ThriftServerList;
import com.thrift4j.client.etcd.EtcdNotificationUpdate;

import mousio.etcd4j.EtcdClient;


public class RibbonRouter implements Router {

  private final String className;

  private final EtcdClient etcdClient;

  private DynamicServerListLoadBalancer<ThriftServer> loadBalancer;

  public RibbonRouter(String className, EtcdClient etcdClient) {
    this.className = className;
    this.etcdClient = etcdClient;
    init();
  }

  @Override
  public void init() {
    DefaultClientConfigImpl config = DefaultClientConfigImpl.getClientConfigWithDefaultValues();
    config.setProperty(CommonClientConfigKey.ServerListUpdaterClassName,
        EtcdNotificationUpdate.class.getName());

    String path = "/dragon/service/" + className;
    loadBalancer = new DynamicServerListLoadBalancer<>(config, new AvailabilityFilteringRule(),
        new DummyPing(), new ThriftServerList(etcdClient, className),
        new ZoneAffinityServerListFilter<>(),
        new EtcdNotificationUpdate(etcdClient, path));
  }

  @Override
  public Node getTransportNode() {
    Server server = loadBalancer.chooseServer();
    if(server == null) {
      return null;
    }
    Node node = new Node();
    node.setIp(server.getHost());
    node.setPort(server.getPort());
    return node;
  }

  public DynamicServerListLoadBalancer<ThriftServer> getLoadBalancer() {
    return loadBalancer;
  }
}
