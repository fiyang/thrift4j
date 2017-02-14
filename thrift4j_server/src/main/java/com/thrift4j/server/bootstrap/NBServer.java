package com.thrift4j.server.bootstrap;

import java.util.ArrayList;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportFactory;

import com.thrift4j.server.config.ServiceProviderConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author fiyang NoBlocking server implements
 */
@Slf4j
public class NBServer {

	private Thread serverThread;
	private TServer server;
	private int port;

	public NBServer(ArrayList<ServiceProviderConfig> providerList, int port) {
		this.port = port;
		this.providerList = providerList;
		for (ServiceProviderConfig config : providerList) {
			this.processor.registerProcessor(config.getName(), config.getProcessor());
		}
	}

	private TMultiplexedProcessor processor = new TMultiplexedProcessor();

	private ArrayList<ServiceProviderConfig> providerList;

	
	public ArrayList<ServiceProviderConfig> getProviderList() {
		return providerList;
	}

	public boolean isServing(){
		return server.isServing();
	}
	
	public void stopServer(){
		if(null != server){
			server.stop();
		}else{
			log.warn("stop server fail server is null");
		}
	}
	
	public void startServer() throws Exception {
		serverThread = new Thread() {
			public void run() {
				try {
					// Transport
					TProtocolFactory tProtocolFactory = new TCompactProtocol.Factory();
					TTransportFactory tTransportFactory = new TFastFramedTransport.Factory();
					TNonblockingServerSocket tNonblockingServerSocket = new TNonblockingServerSocket(
							new TNonblockingServerSocket.NonblockingAbstractServerSocketArgs().port(port));
					TThreadedSelectorServer.Args tThreadedSelectorServerArgs = new TThreadedSelectorServer.Args(
							tNonblockingServerSocket);
					tThreadedSelectorServerArgs.processor(processor);
					tThreadedSelectorServerArgs.protocolFactory(tProtocolFactory);
					tThreadedSelectorServerArgs.transportFactory(tTransportFactory);

					server = new TThreadedSelectorServer(tThreadedSelectorServerArgs);
					log.info("begin to to start service");
					server.serve();
				} catch (Exception e) {
					log.error("NBServer.start Server", e);
				}
			}
		};
		serverThread.start();
		serverThread.setName("NBServer-starter");
		// Run it
		log.info("start server on port {}", port);
		for (ServiceProviderConfig serviceProviderConfig : providerList) {
			log.info("export service ", serviceProviderConfig.getName());
		}
	}
	
}
