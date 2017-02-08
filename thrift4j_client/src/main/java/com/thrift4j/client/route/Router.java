package com.thrift4j.client.route;


public interface Router {
  void init();

  Node getTransportNode();
}
