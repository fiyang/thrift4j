package com.thrift4j.client.config;




import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.thrift4j.client.annotation.ThriftClient;
import com.thrift4j.client.exception.NoAvailableTransportException;
import com.thrift4j.client.exception.ThriftClientException;
import com.thrift4j.client.route.DirectRouter;
import com.thrift4j.client.route.Node;
import com.thrift4j.client.route.RibbonRouter;
import com.thrift4j.client.route.Router;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;
import mousio.etcd4j.EtcdClient;


@Component
@Configuration
//@ConditionalOnClass(ThriftClient.class)
//@Import({ThriftClientConfiguration.class, EtcdAutoConfiguration.class})
@Slf4j
public class ProxyBeanFactory implements BeanPostProcessor {

  private Map<String, Class> beansToProcess = new HashMap<>();

  private Map<String, ThriftClientConfig> thriftClientMap = new ConcurrentHashMap<>();

  @Autowired
  private DefaultListableBeanFactory beanFactory;

  @Autowired
  private GenericKeyedObjectPool<Node, TTransport> pool;

  @Autowired
  private EtcdClient etcdClient;


  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    Class clazz = bean.getClass();
    do {
      for (Field field : clazz.getDeclaredFields()) {
        if (field.isAnnotationPresent(ThriftClient.class)) {
          beansToProcess.put(beanName, clazz);
        }
      }
      for (Method method : clazz.getDeclaredMethods()) {
        if (method.isAnnotationPresent(ThriftClient.class) && method.getParameterCount() == 1) {
          beansToProcess.put(beanName, clazz);
        }
      }
      clazz = clazz.getSuperclass();
    } while (clazz != null);
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (beansToProcess.containsKey(beanName)) {
      Object target = getTargetBean(bean);
      Class clazz = beansToProcess.get(beanName);
      for (Field field : clazz.getDeclaredFields()) {
        ThriftClient annotation = AnnotationUtils.getAnnotation(field, ThriftClient.class);

        if (annotation != null) {
          if (beanFactory.containsBean(field.getName())) {
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, target, beanFactory.getBean(field.getName()));
          } else {
            String realClassName = getRealClassName(field.getType());

            ThriftClientConfig thriftClientBean = createThriftClientBean(field.getType(), realClassName,
                annotation);
            thriftClientMap.put(beanName + "-" + realClassName, thriftClientBean);
            ProxyFactory proxyFactory = getProxyFactoryForThriftClient(target, field.getType(), field.getName());

            addPoolAdvice(proxyFactory, beanName + "-" + realClassName);

            proxyFactory.setFrozen(true);
            proxyFactory.setProxyTargetClass(true);

            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, target, proxyFactory.getProxy());
          }
        }
      }

      for (Method method : clazz.getDeclaredMethods()) {
        ThriftClient annotation = AnnotationUtils.getAnnotation(method, ThriftClient.class);

        if (annotation != null && method.getParameterCount() == 1) {
          Parameter parameter = method.getParameters()[0];

          String realClassName = getRealClassName(parameter.getType());

          ThriftClientConfig thriftClientBean = createThriftClientBean(parameter.getType(), realClassName,
              annotation);
          thriftClientMap.put(beanName + "-" + realClassName, thriftClientBean);
          ProxyFactory proxyFactory = getProxyFactoryForThriftClient(target, parameter.getType(), method.getName());

          addPoolAdvice(proxyFactory, beanName + "-" + realClassName);

          proxyFactory.setFrozen(true);
          proxyFactory.setProxyTargetClass(true);

          ReflectionUtils.makeAccessible(method);
          ReflectionUtils.invokeMethod(method, target, proxyFactory.getProxy());
        }
      }
    }
    return bean;
  }

  private String getRealClassName(Class<?> clazz) {
    String className = clazz.getCanonicalName();
    int lastComma = className.lastIndexOf(".");
    return className.substring(0, lastComma);
  }

  private ThriftClientConfig createThriftClientBean(Class<?> type, String className,
      ThriftClient annotation) {
	  ThriftClientConfig thriftClientBean = new ThriftClientConfig();

    Router router;
    if (annotation.address().isEmpty()) {
      router = new RibbonRouter(className, etcdClient);
    } else {
      router = new DirectRouter(annotation.address());
    }

    thriftClientBean.setRouter(router);
    thriftClientBean.setTimeout(annotation.timeout());
    thriftClientBean.setRetryTimes(annotation.retryTimes());

    try {
      Constructor<?> clientConstructor = type.getConstructor(TProtocol.class);
      thriftClientBean.setClientConstructor(clientConstructor);
    } catch (SecurityException | NoSuchMethodException e) {
      log.error(e.getMessage(), e);
      throw new ThriftClientException(
          ExceptionUtils.getMessage(e) + ", client class name is " + className);
    }

    return thriftClientBean;
  }

  private Object getTargetBean(Object bean) {
    Object target = bean;
    try {
      while (AopUtils.isAopProxy(target)) {
        target = ((Advised) target).getTargetSource().getTarget();
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new ThriftClientException("get target bean error", e);
    }
    return target;
  }

  private ProxyFactory getProxyFactoryForThriftClient(Object bean, Class<?> type, String name) {
    ProxyFactory proxyFactory;
    try {
      proxyFactory = new ProxyFactory(BeanUtils
          .instantiateClass(type.getConstructor(TProtocol.class), (TProtocol) null));
    } catch (NoSuchMethodException e) {
      log.error(e.getMessage(), e);
      throw new InvalidPropertyException(bean.getClass(), name, e.getMessage());
    }
    return proxyFactory;
  }

  @SuppressWarnings("unchecked")
  private void addPoolAdvice(ProxyFactory proxyFactory, String beanName) {
    proxyFactory.addAdvice((MethodInterceptor) methodInvocation -> {
      Object[] args = methodInvocation.getArguments();

      ThriftClientConfig thriftClientBean = thriftClientMap.get(beanName);
      int index = 0;
      while (index++ < thriftClientBean.getRetryTimes()) {
        Node node = null;
        TTransport transport = null;

        try {
          node = thriftClientBean.getRouter().getTransportNode();
          if (node == null) {
            throw new ThriftClientException(
                "no available transport node, bean name is " + beanName);
          }

          node.setTimeout(thriftClientBean.getTimeout());
          transport = pool.borrowObject(node);

          TProtocol protocol = new TBinaryProtocol(transport, true, true);
          Object client = thriftClientBean.getClientConstructor().newInstance(protocol);

          return ReflectionUtils.invokeMethod(methodInvocation.getMethod(), client, args);
        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException | SecurityException | NoSuchMethodException e) {
          throw new ThriftClientException(
              ExceptionUtils.getMessage(e) + ", bean name is " + beanName, e);
        } catch (UndeclaredThrowableException e) {
          if (e.getUndeclaredThrowable() instanceof TTransportException) {
            TTransportException innerException = (TTransportException) e.getUndeclaredThrowable();
            Throwable realException = innerException.getCause();
            if (realException instanceof SocketTimeoutException) { // 超时,直接抛出异常,不进行重试
              if (transport != null) {
                transport.close();
              }
              throw new ThriftClientException(
                  ExceptionUtils.getMessage(e) + ", bean name is " + beanName, e);
            } else if (realException == null) {
              if (innerException.getType() == TTransportException.END_OF_FILE) {
                pool.clear(node);// 把以前的对象池进行销毁
                if (transport != null) {
                  transport.close();
                }
                handlerException(index, thriftClientBean.getRetryTimes(), beanName, e);
                continue;
              } else {
                handlerException(index, thriftClientBean.getRetryTimes(), beanName, e);
                continue;
              }
            } else if (realException instanceof SocketException) {
              pool.clear(node);// 把以前的对象池进行销毁
              if (transport != null) {
                transport.close();
              }
              handlerException(index, thriftClientBean.getRetryTimes(), beanName, e);
              continue;
            } else {
              handlerException(index, thriftClientBean.getRetryTimes(), beanName, e);
              continue;
            }
            // 有可能服务端返回的结果里面存在null
          } else if (e.getUndeclaredThrowable() instanceof TApplicationException) {
            throw new ThriftClientException(
                ExceptionUtils.getMessage(e) + ", bean name is " + beanName, e);
            // 未知的情况
          } else {
            throw new ThriftClientException(
                ExceptionUtils.getMessage(e) + ", bean name is " + beanName, e);
          }
        } catch (Exception e) {
          if (e.getCause() instanceof NoAvailableTransportException) { // 没有可用的节点，直接抛出
            throw new ThriftClientException(
                ExceptionUtils.getMessage(e) + ", bean name is " + beanName, e);
          } else {
            handlerException(index, thriftClientBean.getRetryTimes(), beanName, e);
            continue;
          }
        } finally {
          try {
            if (pool != null && transport != null) {
              pool.returnObject(node, transport);
            }
          } catch (Exception e) {
          }
        }
      }
      throw new ThriftClientException("rpc client call failed, bean name is " + beanName);
    });
  }

  private void handlerException(int index, int retryTimes, String beanName, Throwable t) {
    if (index == retryTimes) {
      throw new ThriftClientException(ExceptionUtils.getMessage(t) + ", bean name is " + beanName,
          t);
    }
  }


  @PreDestroy
  public void destroy() {
    thriftClientMap.forEach((k, v) -> {
      if (v.getRouter() instanceof RibbonRouter) {
        ((RibbonRouter) v.getRouter()).getLoadBalancer().shutdown();
      }
    });
  }

}

