package com.thrift4j.server.bootstrap;

import java.util.ArrayList;

import com.thrift4j.server.config.ServiceProviderConfig;

public class GlobalContext {
	
	private ArrayList<ServiceProviderConfig> providerList;
	
	public void setProviderList(ArrayList<ServiceProviderConfig> providerList) {
		this.providerList = providerList;
	}


	public ArrayList<ServiceProviderConfig> getProviderList() {
		return providerList;
	}

	static class GlobalContextHolder{
		public static GlobalContext instance = new GlobalContext();
	}
	
	private GlobalContext(){
	}
	
	public static GlobalContext getInstance(){
		return GlobalContextHolder.instance;
	}
}
