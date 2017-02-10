package thrift4j_etcd;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.thrift4j.etcd.configure.EtcdClientProperties;

import junit.framework.TestCase;



public class EtcdTestMain extends TestCase{
	 public static final String DEFAULT_SPRING_CONFIG = "classpath*:META-INF/spring/*.xml";

	 static ClassPathXmlApplicationContext context;
	
	public void testInit() {
		context = new ClassPathXmlApplicationContext(DEFAULT_SPRING_CONFIG);
		EtcdClientProperties properties = (EtcdClientProperties) context.getBean("etcdClientProperties");
		assertNotNull(properties);
		assertTrue(properties.getUris().size() == 3);
	}
}
