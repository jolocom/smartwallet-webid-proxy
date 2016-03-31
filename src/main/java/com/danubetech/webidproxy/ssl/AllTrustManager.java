/*
 *  WebID-TLS Proxy
 *  Copyright (C) 2016 Danube Tech GmbH
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
