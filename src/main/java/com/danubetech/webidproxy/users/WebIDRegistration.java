package com.danubetech.webidproxy.users;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import com.danubetech.webidproxy.ssl.MySSLSocketFactory;

public class WebIDRegistration {

	private static final Log log = LogFactory.getLog(WebIDRegistration.class);

	public static final String baseEndpoint = "https://localhost:8443/";
	public static final String accountEndpoint = baseEndpoint + ",system/newAccount";
	public static final String certEndpoint = baseEndpoint + ",system/newCert";

	static void registerWebIDAccount(User user) throws IOException {

		List<NameValuePair> accountParameterMap = new ArrayList<NameValuePair> ();
		accountParameterMap.add(new BasicNameValuePair("username", user.getUsername()));
		accountParameterMap.add(new BasicNameValuePair("spkac", user.getSpkac()));
		if (user.getName() != null) accountParameterMap.add(new BasicNameValuePair("name", user.getName()));
		if (user.getEmail() != null) accountParameterMap.add(new BasicNameValuePair("email", user.getEmail()));

		submit(accountEndpoint, accountParameterMap);
	}

	static void registerWebIDCert(User user) throws IOException {

		String webid = baseEndpoint + user.getUsername() + "/profile/card#me";

		List<NameValuePair> certParameterMap = new ArrayList<NameValuePair> ();
		certParameterMap.add(new BasicNameValuePair("webid", webid));
		certParameterMap.add(new BasicNameValuePair("spkac", user.getSpkac()));
		certParameterMap.add(new BasicNameValuePair("name", "My WebID account"));
		submit(certEndpoint, certParameterMap);
	}

	private static void submit(String target, List<? extends NameValuePair> nameValuePairs) throws IOException {

		HttpClient httpClient = MySSLSocketFactory.getNewHttpClient(null);
		HttpPost httpPost = new HttpPost(target);
		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		HttpResponse httpResponse = httpClient.execute(httpPost);

		log.info("SUBMIT " + target + " -> " + httpResponse.getStatusLine());
	}
}
