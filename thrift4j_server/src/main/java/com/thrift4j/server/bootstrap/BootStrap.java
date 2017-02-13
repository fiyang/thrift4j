package com.thrift4j.server.bootstrap;


import org.springframework.context.SmartLifecycle;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BootStrap implements SmartLifecycle{
	
	  private NBServer server;

	  private int phase = Integer.MAX_VALUE;

	  @Override
	  public boolean isAutoStartup() {
	    return true;
	  }

	  @Override
	  public void stop(Runnable runnable) {
	    if (isRunning()) {
	      log.info("thrift server shutdown");
	      server.stopServer();
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

	public static void main(String [] args){
		
	}
}
