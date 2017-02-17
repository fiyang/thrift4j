package com.thrift4j.client.connection;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.facebook.fb303.FacebookService.Iface;
import com.thrift4j.client.route.Node;
import com.thrift4j.client.util.IFaceQuene;
import com.thrift4j.client.util.TQuene;


public class TMultiplexedProtocolBuilder {
	
	public static TQuene build(ServerConfig config) throws TTransportException {
		List<Node> nodeList = config.getNodeList();
		TQuene tquene = new TQuene(nodeList.size(),100);
		for (Node node : nodeList) {
			TSocket socket = new TSocket(node.getIp(), node.getPort());
			tquene.add(socket);
			
		}
		return tquene;
	}
	
	public static IFaceQuene buildIface(ServerConfig config) throws TTransportException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		List<Node> nodeList = config.getNodeList();
		IFaceQuene ifaceQuene = new IFaceQuene(nodeList.size(),100);
		for (Node node : nodeList) {
			
			/*
			tsocket.open();
			TTransport transport = tsocket;
		    transport = new TFramedTransport(transport);
		    TProtocol tProtocol = new TCompactProtocol(transport);
			*/
			TSocket socket = new TSocket(node.getIp(), node.getPort());
			socket.open();
			TTransport transport = socket;
		    transport = new TFramedTransport(transport);
		    TProtocol tProtocol = new TCompactProtocol(transport);
		    Class clazz = node.getClazz();
		    Class interfaceClass =  clazz.getDeclaringClass();
		    TMultiplexedProtocol cmccProocol = new  TMultiplexedProtocol(tProtocol,interfaceClass.getCanonicalName());
			
		    @SuppressWarnings("unchecked")
			Iface iface = (Iface) clazz.getConstructor(org.apache.thrift.protocol.TProtocol.class).newInstance(cmccProocol);
		    ifaceQuene.add(iface);
		}
		return ifaceQuene;
	}
	
	
}
