package com.thrift4j.client.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.transport.TSocket;

public class TQuene extends LinkedBlockingQueue<TSocket>{

	    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		private int _duration;
	    private int _minConn;
	    private final Object shrinkLockHelper = new Object();
	    private CopyOnWriteArrayList<TSocket> _AllSocket = new CopyOnWriteArrayList<TSocket>();
	    
	    public TQuene(int _duration, int _minConn) {
	        this._duration = _duration;
	        this._minConn = _minConn;
	    }

	    public TSocket enqueue(TSocket element) {
	        offer(element);
	        return element;
	    }

	    public TSocket dequeue() {
	    	TSocket csocket = (TSocket) poll();
	        return csocket;
	    }
	    
	    public TSocket dequeue(long time) throws InterruptedException {
	    	TSocket csocket = (TSocket) poll(time,TimeUnit.MILLISECONDS);
	        return csocket;
	    }

	    public void register(TSocket socket) {
	        _AllSocket.add(socket);
	    }

	    public synchronized boolean remove(TSocket socket) {
	        _AllSocket.remove(socket);
	        return super.remove(socket);
	    }

	    public int getTotal() {
	        return _AllSocket.size();
	    }

	    public List<TSocket> getAllSocket() {
	        return _AllSocket;
	    }
	    private long lastCheckTime = System.currentTimeMillis();
	    private int freeCount = -1;
	    private int shrinkCount = 0;

	    public boolean shrink() {
	        synchronized (shrinkLockHelper) {
	            if (shrinkCount > 0) {
	                shrinkCount--;
	                return true;
	            }
	            if ((System.currentTimeMillis() - lastCheckTime) > _duration) {
	                lastCheckTime = System.currentTimeMillis();
	                boolean b = (freeCount > 0) && (getTotal() > _minConn);
	                if (b) {
	                    shrinkCount = Math.min((getTotal() - _minConn), freeCount);
	                    if (shrinkCount < 0) {
	                        shrinkCount = 0;
	                    }
	                }
	                return false;
	            }
	            int currFreeCount = this.size();
	            if (currFreeCount < freeCount || freeCount < 0) {
	                freeCount = currFreeCount;
	            }
	            return false;
	        }
	    }
}
