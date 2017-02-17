package com.thrift4j.client.route;

import java.lang.reflect.Method;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Data
@EqualsAndHashCode(exclude = {"timeout"})
@ToString
public class Node {
  private String ip;
  private int port;
  private int timeout = 500;
  
  private Class clazz;
  private Method method;
}
