package com.danubetech.webidproxy.config;

public class Config {

	public static final String DEFAULT_WEBIDHOST = "localhost:8443";
	
	public static final String webidHost() {
		
		String webidHost = System.getProperty("webid.host");
		if (webidHost != null) return webidHost;
		
		return DEFAULT_WEBIDHOST;
	}
}
