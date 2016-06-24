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
import java.io.File;
import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

import com.danubetech.webidproxy.users.User;

public class MySSLSocketFactory {

	private static final Log log = LogFactory.getLog(MySSLSocketFactory.class);

	private static final String CLIENT_KEYSTORE_PASS = "changeit";

	public static CloseableHttpClient getNewHttpClient(HttpServletRequest request, User user) {

		CloseableHttpClient httpClient = request == null ? null : (CloseableHttpClient) request.getSession().getAttribute("HTTPCLIENT");
		if (httpClient != null) {

			log.info("Retrieved HTTPCLIENT from session.");
			return httpClient;
		}

		File keyfile = user == null ? null : new File("./users/" + user.getUsername() + ".p12");

		SSLContext sslContext;

		try {

			SSLContextBuilder sslContextBuilder = SSLContexts.custom();
			if (keyfile != null) sslContextBuilder.loadKeyMaterial(keyfile, CLIENT_KEYSTORE_PASS.toCharArray(), CLIENT_KEYSTORE_PASS.toCharArray());
			sslContextBuilder.loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE);
			sslContextBuilder.useProtocol("TLS");
			sslContextBuilder.setSecureRandom(null);
			sslContext = sslContextBuilder.build();
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}

		SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
				sslContext,
				new NoopHostnameVerifier());

		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.getSocketFactory())
				.register("https", sslConnectionSocketFactory)
				.build();

		SchemePortResolver schemePortResolver = new DefaultSchemePortResolver();

		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		httpClientBuilder.setSSLSocketFactory(sslConnectionSocketFactory);
		httpClientBuilder.setConnectionManager(new PoolingHttpClientConnectionManager(registry));
		httpClientBuilder.setSchemePortResolver(schemePortResolver);


		RFC6265CookieSpecProvider cookieSpecProvider = new RFC6265CookieSpecProvider();
		Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
				.register(CookieSpecs.DEFAULT, cookieSpecProvider)
				.register(CookieSpecs.STANDARD, cookieSpecProvider)
				.register(CookieSpecs.STANDARD_STRICT, cookieSpecProvider)
				.build();

		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
		requestConfigBuilder.setCookieSpec(CookieSpecs.DEFAULT);

		RequestConfig requestConfig = requestConfigBuilder.build();

		httpClientBuilder.setDefaultRequestConfig(requestConfig);
		httpClientBuilder.setDefaultCookieSpecRegistry(cookieSpecRegistry);
		httpClientBuilder.addInterceptorLast(MYHTTPREQUESTINTERCEPTOR);
		httpClientBuilder.addInterceptorFirst(MYHTTPRESPONSEINTERCEPTOR);

		httpClient = httpClientBuilder.build();
		if (request != null) request.getSession().setAttribute("HTTPCLIENT", httpClient);
		log.info("Storing HTTPCLIENT in session.");

		return httpClient;
	}

	private static HttpRequestInterceptor MYHTTPREQUESTINTERCEPTOR = new HttpRequestInterceptor() {

		@Override
		public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {

			log.debug("<< REQUEST: " + request.getRequestLine());

			for (Header header : request.getAllHeaders()) {

				log.debug("<< HEADER: " + header.getName() + " -> " + header.getValue());
			}
		}
	};

	private static HttpResponseInterceptor MYHTTPRESPONSEINTERCEPTOR = new HttpResponseInterceptor() {

		@Override
		public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {

			log.debug(">> RESPONSE: " + response.getStatusLine());

			for (Header header : response.getAllHeaders()) {

				log.debug(">> HEADER: " + header.getName() + " -> " + header.getValue());
			}
		}
	};
}
