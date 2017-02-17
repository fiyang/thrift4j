package com.thrift4j.server.config;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.apache.thrift.TProcessor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ClassUtils;

import com.thrift4j.server.annotation.ThriftService;
import com.thrift4j.server.bootstrap.GlobalContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplicationScanner implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		try{
			log.info("set appcontext ok");
			//利用反射查找 
			ArrayList<ServiceProviderConfig> serviceAllList = scan();
			//存入全局上下文
			GlobalContext.getInstance().setProviderList(serviceAllList);
		}catch(Exception e){
			log.error("application init fail ",e);
			log.error("=============system provider error==========================system while return");
			System.exit(-1);
		}
	}

	private ArrayList<ServiceProviderConfig> scan() throws NoSuchMethodException, SecurityException {
		String[] beanNames = applicationContext.getBeanNamesForAnnotation(ThriftService.class);
		if (beanNames != null && beanNames.length > 0) {
			ArrayList<ServiceProviderConfig> list = new ArrayList<ServiceProviderConfig>();
			for (String string : beanNames) {
				list.add(getConfigByName(string));
			}
			return list;
		}
		return null;
	}

	public ServiceProviderConfig getConfigByName(String className) throws NoSuchMethodException, SecurityException{
		Object bean = applicationContext.getBean(className);
	      Class<?> serviceClass = null;
	      Class<TProcessor> processorClass = null;
	      Class<?> ifaceClass = null;
	      Class<?>[] handlerInterfaces = ClassUtils.getAllInterfaces(bean);
	      
	      for (Class<?> interfaceClass : handlerInterfaces) {
	        if (!interfaceClass.getName().endsWith("$Iface")) {
	          continue;
	        }
	        serviceClass = interfaceClass.getDeclaringClass();
	        if (serviceClass == null) {
	          continue;
	        }
	        for (Class<?> innerClass : serviceClass.getDeclaredClasses()) {
	          if (!innerClass.getName().endsWith("$Processor")) {
	            continue;
	          }
	          if (!TProcessor.class.isAssignableFrom(innerClass)) {
	            continue;
	          }
	          if (ifaceClass != null) {
	            throw new IllegalStateException("Multiple Thrift Ifaces defined on handler");
	          }
	          ifaceClass = interfaceClass;
	          processorClass = (Class<TProcessor>) innerClass;
	          break;
	        }
	      }
	      if (ifaceClass == null) {
	        log.error("iface class is null");
	        throw new IllegalStateException("No Thrift Ifaces found on handler");
	      }
	      Constructor<TProcessor> processorConstructor = processorClass.getConstructor(ifaceClass);
	      TProcessor processor = BeanUtils.instantiateClass(processorConstructor, bean);
	      ServiceProviderConfig config = new ServiceProviderConfig();
	      config.setProcessor(processor);
	      if(null != serviceClass){
	    	  config.setName(serviceClass.getCanonicalName());
	      }else{
	    	  String name = ifaceClass.getCanonicalName();
	    	  int index = name.indexOf("$Iface");
	    	  name = name.substring(0, index);
	    	  config.setName(name);
	      }
	      return config;
	}
}
