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
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import com.danubetech.webidproxy.WebIDProxyServlet;
import com.danubetech.webidproxy.users.User;

public class MySSLSocketFactory extends SSLSocketFactory {

	private static final Log log = LogFactory.getLog(MySSLSocketFactory.class);

	private static final String CLIENT_KEYSTORE_TYPE = "PKCS12";
	private static final String CLIENT_KEYSTORE_PASS = "changeit";

	private SSLContext sslContext;

	public MySSLSocketFactory(User user) {

		super(init(user));

		sslContext = init(user);
	}

	private static KeyManager[] kms(User user) throws Exception {

		// private key and certificate

		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(user.getPrivatekey().getBytes(Charset.forName("UTF-8"))));
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");

		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		X509Certificate certificate = (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(Base64.decodeBase64(user.getCertificate())));
		
		final PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

		// key manager

		KeyStore ks = KeyStore.getInstance(CLIENT_KEYSTORE_TYPE);
//		ks.load(new FileInputStream(CLIENT_KEYSTORE_PATH), CLIENT_KEYSTORE_PASS.toCharArray());
		ks.load(new FileInputStream("./users/" + user.getUsername() + ".p12"), CLIENT_KEYSTORE_PASS.toCharArray());
//		ks.load(null, CLIENT_KEYSTORE_PASS.toCharArray());
//		ks.setKeyEntry(user.getUsername(), privateKey, CLIENT_KEYSTORE_PASS.toCharArray(), new Certificate[] { certificate });

		KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmfactory.init(ks, CLIENT_KEYSTORE_PASS.toCharArray());

/*		KeyManager km = new X509ExtendedKeyManager() {

			@Override
			public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socker) {

				return null;
			}

			@Override
			public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {

				return null;
			}

			@Override
			public X509Certificate[] getCertificateChain(String alias) {

				return null;
			}

			@Override
			public String[] getClientAliases(String keyType, Principal[] issuers) {

				return null;
			}

			@Override
			public PrivateKey getPrivateKey(String alias) {

				return privateKey;
			}

			@Override
			public String[] getServerAliases(String keyType, Principal[] issuers) {

				return null;
			}
		};*/

		KeyManager[] kms = kmfactory.getKeyManagers();
		//KeyManager[] kms = new KeyManager[] { km };

		if (log.isDebugEnabled()) log.debug("KM: " + kms[0]);
		return kms;
	}

	private static TrustManager[] tms() throws Exception {

		// trust manager

		KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
		ts.load(null, null);

		TrustManager tm = new X509TrustManager() {

			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

			}

			public X509Certificate[] getAcceptedIssuers() {

				return null;
			}
		};

		TrustManager[] tms = new TrustManager[] { tm };

		if (log.isDebugEnabled()) log.debug("TM: " + tms[0]);
		return tms;
	}

	private static SSLContext init(User user) {

		try {

			// ssl context

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(user != null ? kms(user) : null, tms(), null);
			if (log.isDebugEnabled()) log.debug("SSL Context: " + sslContext);

			// done

			return sslContext;
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static HttpClient getNewHttpClient(User user) {

		try {

			MySSLSocketFactory sf = new MySSLSocketFactory(user);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {

			return new DefaultHttpClient();
		}
	}	

	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {

		return this.sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	}

	@Override
	public Socket createSocket() throws IOException {

		return this.sslContext.getSocketFactory().createSocket();
	}
}