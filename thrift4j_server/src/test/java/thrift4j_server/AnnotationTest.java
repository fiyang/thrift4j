package thrift4j_server;

import java.util.concurrent.CountDownLatch;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.thrift4j.etcd.configure.EtcdClientProperties;
import com.thrift4j.etcd.register.EtcdRegister;
import com.thrift4j.server.bootstrap.GlobalContext;
import com.thrift4j.server.bootstrap.NBServer;

import junit.framework.TestCase;

public class AnnotationTest extends TestCase{
	
	
	public static final String DEFAULT_SPRING_CONFIG = "/opt/apps/app/servertest/applicationContext.xml";

	static ClassPathXmlApplicationContext context;
	
	public static void main(String [] args) throws Exception{
		context = new ClassPathXmlApplicationContext(DEFAULT_SPRING_CONFIG);
		//EtcdClientProperties client = (EtcdClientProperties) context.getBean("etcdClientProperties");
		//assertNotNull(client);
		EtcdRegister register = (EtcdRegister)context.getBean("etcdRegister");
		System.out.println(register.getKey());
		System.out.println(register.getPath());
		System.out.println(register.getValue());
		
		NBServer nbServer = new NBServer(GlobalContext.getInstance().getProviderList(), 8091);
		nbServer.startServer();
		
		//context.getBean("userServiceImpl");
		CountDownLatch latch = new CountDownLatch(1);
		//System.out.println("ok master thread begin to wait");
		latch.await();
	}
	
	public void testAnnotation() throws Exception{
		context = new ClassPathXmlApplicationContext(DEFAULT_SPRING_CONFIG);
		//EtcdClientProperties client = (EtcdClientProperties) context.getBean("etcdClientProperties");
		//assertNotNull(client);
		NBServer nbServer = new NBServer(GlobalContext.getInstance().getProviderList(), 8091);
		nbServer.startServer();
		
		EtcdRegister register = (EtcdRegister)context.getBean("etcdRegister");
		assertNotNull(register);
		System.out.println(register.getKey());
		System.out.println(register.getPath());
		System.out.println(register.getValue());
		
		//context.getBean("userServiceImpl");
		CountDownLatch latch = new CountDownLatch(1);
		//System.out.println("ok master thread begin to wait");
		latch.await();
		
		/*
		EtcdClientProperties properties = (EtcdClientProperties) context.getBean("etcdClientProperties");
		assertNotNull(properties);
		assertTrue(properties.getUris().size() == 3);
		*/
		
	}
}
