package com.thrift4j.client.connection;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.facebook.fb303.FacebookService.Iface;
import com.facebook.fb303.fb_status;
import com.thrift4j.client.route.Node;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IfacePoolFactory {

	private GenericObjectPool<Iface> pool;  
	  
    public IfacePoolFactory(GenericObjectPoolConfig config, Node node) {  
    	ConnectionFactory objFactory = new ConnectionFactory(node);  
        pool = new GenericObjectPool<Iface>(objFactory, config);  
    }  
    
    //从池里获取一个Transport对象
    public Iface getConnection() throws Exception {  
        return pool.borrowObject();  
    }  
    
    //把一个Transport对象归还到池里
    public void releaseConnection(Iface transport) {  
        pool.returnObject(transport);   
    }  
    
    /*
     * 连接池管理的对象Transport的工厂类，
     * GenericObjectPool会使用此类的create方法来
     * 创建Transport对象并放进pool里进行管理等操作。
     */
    class ConnectionFactory extends BasePooledObjectFactory<Iface> {   
    	private Node node;
          
        public ConnectionFactory(Node node) {  
        	this.node = node;
        }  
		@Override
		public boolean validateObject(PooledObject<Iface> p) {
			// TODO Auto-generated method stub
			try {
				p.getObject();
				fb_status fbstatus = p.getObject().getStatus();
				if(fbstatus.getValue() == fb_status.ALIVE.getValue()){
					return true;
				}else{
					return false;
				}
			} catch (TException e) {
				e.printStackTrace();
			}
			return false;
		}
		@Override
		public Iface create() throws Exception {
        	TSocket socket = new TSocket(node.getIp(), node.getPort());
        	socket.setTimeout(node.getTimeout());
        	
			socket.open();
			TTransport transport = socket;
		    transport = new TFramedTransport(transport);
		    TProtocol tProtocol = new TCompactProtocol(transport);
		    Class clazz = node.getClazz();
		    Class interfaceClass =  clazz.getDeclaringClass();
		    TMultiplexedProtocol cmccProocol = new  TMultiplexedProtocol(tProtocol,interfaceClass.getCanonicalName());
			
		    @SuppressWarnings("unchecked")
			Iface iface = (Iface) clazz.getConstructor(org.apache.thrift.protocol.TProtocol.class).newInstance(cmccProocol);
			return iface;
		}
        //把TTransport对象打包成池管理的对象PooledObject<TTransport>
		@Override
		public PooledObject<Iface> wrap(Iface iface) {
			return new DefaultPooledObject<Iface>(iface);
		}
  
    }       
}
