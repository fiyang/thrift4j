package thrift4j_client;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;
import mousio.client.retry.RetryNTimes;
import mousio.client.retry.RetryPolicy;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;

public class EtcdClientRetryTest extends TestCase{
	public void testRetryClient() throws IOException, EtcdException, EtcdAuthenticationException, TimeoutException{
		EtcdClient etcd = new EtcdClient(URI.create("http://127.0.0.1:2379"));
		//System.out.println(etcd.getVersion());
		
		//EtcdKeyGetRequest request = etcd.get("mykeys");
		//request.
		/*
		try(EtcdClient etcd = new EtcdClient(
			    URI.create("http://127.0.0.1:2379"))){
			    //URI.create("http://123.45.67.90:8001"))){
			  // Logs etcd version
			  System.out.println(etcd.getVersion());
			}
		*/
		etcd.setRetryHandler(new RetryNTimes(10,2));
		EtcdKeysResponse response = etcd.put("foo", "bar").send().get();
		System.out.println(response.getNode().getValue());
	}
}
