package com.jolocom.webidproxy.users;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import com.jolocom.webidproxy.config.Config;
import com.jolocom.webidproxy.ssl.MySSLSocketFactory;

public class WebIDRegistration {

	private static final Log log = LogFactory.getLog(WebIDRegistration.class);

	public static void registerWebIDAccount(User user) throws IOException {

		String webid = webid(user);
		String host = host(user);

		List<NameValuePair> accountParameterMap = new ArrayList<NameValuePair> ();
		accountParameterMap.add(new BasicNameValuePair("username", user.getUsername()));
		accountParameterMap.add(new BasicNameValuePair("spkac", user.getSpkac()));
		accountParameterMap.add(new BasicNameValuePair("webid", webid));
		accountParameterMap.add(new BasicNameValuePair("host", host));
		if (user.getName() != null) accountParameterMap.add(new BasicNameValuePair("name", user.getName()));
		if (user.getEmail() != null) accountParameterMap.add(new BasicNameValuePair("email", user.getEmail()));

		post(null, null, accountEndpoint(user), accountParameterMap);
	}

	public static void newWebIDCert(HttpServletRequest request, User user) throws IOException {

		List<NameValuePair> certParameterMap = new ArrayList<NameValuePair> ();
		certParameterMap.add(new BasicNameValuePair("username", user.getUsername()));
		certParameterMap.add(new BasicNameValuePair("spkac", user.getSpkac()));
		certParameterMap.add(new BasicNameValuePair("webid", user.getWebid()));

		post(request, user, certEndpoint(user), certParameterMap);
	}

	private static String webid(User user) {

		if (Config.vhosts()) {

			return "https://" + user.getUsername() + "." + Config.webidHost() + "/profile/card#me";
		} else {

			return "https://" + Config.webidHost() + "/" + user.getUsername() + "/profile/card#me";
		}
	}

	private static String host(User user) {

		if (Config.vhosts()) {

			return user.getUsername() + "." + Config.webidHost();
		} else {

			return Config.webidHost();
		}
	}

	private static String accountEndpoint(User user) {

		if (Config.vhosts()) {

			return "https://" + user.getUsername() + "." + Config.webidHost() + "/" + "api/accounts/new";
		} else {

			return "https://" + Config.webidHost() + "/" + "api/accounts/new";
		}
	}

	private static String certEndpoint(User user) {

		if (Config.vhosts()) {

			return "https://" + user.getUsername() + "." + Config.webidHost() + "/" + "api/accounts/cert";
		} else {

			return "https://" + Config.webidHost() + "/" + "api/accounts/cert";
		}
	}

	private static void post(HttpServletRequest request, User user, String target, List<? extends NameValuePair> nameValuePairs) throws IOException {

		HttpClient httpClient = MySSLSocketFactory.getNewHttpClient(request, user);
		HttpPost httpPost = new HttpPost(target);
		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		HttpResponse httpResponse = httpClient.execute(httpPost);

		log.info("SUBMIT " + target + " " + nameValuePairs + " -> " + httpResponse.getStatusLine());

		if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) throw new IOException("" + httpResponse.getStatusLine().getStatusCode() + " " + httpResponse.getStatusLine().getReasonPhrase());
	}
}
