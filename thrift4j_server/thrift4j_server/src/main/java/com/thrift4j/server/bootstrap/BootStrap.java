package com.thrift4j.server.bootstrap;

import org.apache.thrift.server.TServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BootStrap implements SmartLifecycle{
	
	 @Autowired
	  private TServer server;

	  private int phase = Integer.MAX_VALUE;

	  @Override
	  public boolean isAutoStartup() {
	    return true;
	  }

	  @Override
	  public void stop(Runnable runnable) {
	    if (isRunning()) {
	      log.info("thrift server shutdown");
	      //server.setShouldStop(true);
	      server.stop();
	      if (runnable != null) {
	        runnable.run();
	      }
	    }
	  }

	  @Override
	  public void start() {
	    if (server == null) {
	      return;
	    }
	    ThriftServer thriftServer = new ThriftServer(server);
	    Thread runnerThread = new Thread(thriftServer, "thrift server");
	    runnerThread.start();
	  }


	  @Override
	  public void stop() {
	    stop(null);
	  }

	  @Override
	  public boolean isRunning() {
	    if (server != null) {
	      return server.isServing();
	    }
	    return false;
	  }

	  @Override
	  public int getPhase() {
	    return this.phase;
	  }

	  class ThriftServer implements Runnable {

	    private TServer server;

	    ThriftServer(TServer server) {
	      this.server = server;
	    }

	    @Override
	    public void run() {
	      if (server != null) {
	        log.info("thrift server started");
	        this.server.serve();
	      }
	    }
	  }
	
	public static void main(String [] args){
		
	}
}
