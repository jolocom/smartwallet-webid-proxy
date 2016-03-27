package com.danubetech.webidproxy.ssl;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * DO NOT USE THIS IN A PRODUCTION ENVIRONMENT !!
 */
public class AllTrustManager implements X509TrustManager, HostnameVerifier {

	@Override
	public X509Certificate[] getAcceptedIssuers() { return null; }

	@Override
	public void checkClientTrusted(X509Certificate[] certs, String authType) {

	}

	@Override
	public void checkServerTrusted(X509Certificate[] certs, String authType) {

	}

	@Override
	public boolean verify(String hostname, SSLSession session) {

		return true;
	}

	public static void enable() throws Exception {  

		AllTrustManager allTrustManager = new AllTrustManager();

		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, new TrustManager[] { allTrustManager }, null);
		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(allTrustManager);
	}  
}
