package com.jolocom.webidproxy.users;

import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import com.jolocom.webidproxy.config.Config;
import com.jolocom.webidproxy.ssl.MySSLSocketFactory;

public class WebIDRegistration {

	private static final Log log = LogFactory.getLog(WebIDRegistration.class);

	public static final URI URI_FOAF_MBOX = new URIImpl("http://xmlns.com/foaf/0.1/mbox");

	public static String webidForUsername(String username) {

		if (Config.vhosts()) {

			return "https://" + username + "." + Config.webidHost() + "/profile/card#me";
		} else {

			return "https://" + Config.webidHost() + "/" + username + "/profile/card#me";
		}
	}

	public static void registerWebIDAccount(User user, String email, String spkac, KeyPair keyPair) throws IOException {

		String webid = webid(user);
		String host = host(user);

		List<NameValuePair> accountParameterMap = new ArrayList<NameValuePair> ();
		accountParameterMap.add(new BasicNameValuePair("username", user.getUsername()));
		accountParameterMap.add(new BasicNameValuePair("spkac", spkac));
		accountParameterMap.add(new BasicNameValuePair("webid", webid));
		accountParameterMap.add(new BasicNameValuePair("host", host));
		if (user.getName() != null) accountParameterMap.add(new BasicNameValuePair("name", user.getName()));
		if (email != null) accountParameterMap.add(new BasicNameValuePair("email", email));

		post(null, user, accountEndpoint(user), accountParameterMap, keyPair);
	}

	public static void newWebIDCert(HttpServletRequest request, User user, String spkac, KeyPair keyPair) throws IOException {

		List<NameValuePair> certParameterMap = new ArrayList<NameValuePair> ();
		certParameterMap.add(new BasicNameValuePair("username", user.getUsername()));
		certParameterMap.add(new BasicNameValuePair("spkac", spkac));
		certParameterMap.add(new BasicNameValuePair("webid", user.getWebid()));

		post(request, user, certEndpoint(user), certParameterMap, keyPair);
	}

	public static String retrieveUserEmail(HttpServletRequest request, User user, KeyPair keyPair) throws DataRetrievalException {

		HttpClient httpClient = MySSLSocketFactory.createHttpClient(request, user, keyPair);
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		clientConfiguration.setHttpClient(httpClient);

		LDClient ldclient = new LDClient(clientConfiguration);

		URI subject = new URIImpl(webid(user));
		URI predicate = URI_FOAF_MBOX;

		ClientResponse result = ldclient.retrieveResource(webid(user));
		Iterator<Statement> statements = result.getData().match(subject, predicate, null, new Resource[0]);

		if (! statements.hasNext()) return null;
		Statement statement = statements.next();
		Value value = statement.getObject();
		if (! (value instanceof URI)) return null;
		URI uri = (URI) value;

		String email = uri.stringValue().substring("mailto:".length());
		email = email.replace("%40", "@");

		return email;
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

	private static void post(HttpServletRequest request, User user, String target, List<? extends NameValuePair> nameValuePairs, KeyPair keyPair) throws IOException {

		HttpClient httpClient = MySSLSocketFactory.createHttpClient(request, user, keyPair);
		HttpPost httpPost = new HttpPost(target);
		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		HttpResponse httpResponse = httpClient.execute(httpPost);

		log.info("SUBMIT " + target + " " + nameValuePairs + " -> " + httpResponse.getStatusLine());

		if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) throw new IOException("" + httpResponse.getStatusLine().getStatusCode() + " " + httpResponse.getStatusLine().getReasonPhrase());
	}
}
