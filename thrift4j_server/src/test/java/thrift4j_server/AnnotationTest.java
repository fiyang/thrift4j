package thrift4j_server;

import java.util.concurrent.CountDownLatch;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.thrift4j.etcd.configure.EtcdClientProperties;
import com.thrift4j.server.bootstrap.GlobalContext;
import com.thrift4j.server.bootstrap.NBServer;

import junit.framework.TestCase;

public class AnnotationTest extends TestCase{
	
	
	public static final String DEFAULT_SPRING_CONFIG = "classpath*:META-INF/spring/*.xml";

	static ClassPathXmlApplicationContext context;
	
	
	public void testAnnotation() throws Exception{
		context = new ClassPathXmlApplicationContext(DEFAULT_SPRING_CONFIG);
		//EtcdClientProperties client = (EtcdClientProperties) context.getBean("etcdClientProperties");
		//assertNotNull(client);
		NBServer nbServer = new NBServer(GlobalContext.getInstance().getProviderList(), 8091);
		nbServer.startServer();
		//context.getBean("userServiceImpl");
		CountDownLatch latch = new CountDownLatch(1);
		System.out.println("ok master thread begin to wait");
		latch.await();
		/*
		EtcdClientProperties properties = (EtcdClientProperties) context.getBean("etcdClientProperties");
		assertNotNull(properties);
		assertTrue(properties.getUris().size() == 3);
		*/
		
	}
}
