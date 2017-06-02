package com.jolocom.webidproxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.EntityUtils;

import com.jolocom.webidproxy.ssl.MySSLSocketFactory;
import com.jolocom.webidproxy.users.User;
import com.jolocom.webidproxy.users.Users;
import com.jolocom.webidproxy.users.UsersFileImpl;

public class WebIDProxyServlet extends BaseServlet {

	private static final long serialVersionUID = 3793048689633131588L;

	private static final Log log = LogFactory.getLog(WebIDProxyServlet.class);

	public static final String[] COPY_HEADERS = new String[] { "Accept", "Content-Type", "Origin" };
	public static final Map<String, String> GET_HEADERS;
	public static final Map<String, String> PUT_HEADERS;
	public static final Map<String, String> POST_HEADERS;
	public static final Map<String, String> PATCH_HEADERS;
	public static final Map<String, String> DELETE_HEADERS;
	public static final Map<String, String> OPTIONS_HEADERS;

	static {

		GET_HEADERS = new HashMap<String, String> ();
		PUT_HEADERS = new HashMap<String, String> ();
		POST_HEADERS = new HashMap<String, String> ();
		PATCH_HEADERS = new HashMap<String, String> ();
		DELETE_HEADERS = new HashMap<String, String> ();
		OPTIONS_HEADERS = new HashMap<String, String> ();

		GET_HEADERS.put("Access-Control-Expose-Headers", "Content-Type");
		OPTIONS_HEADERS.put("Access-Control-Allow-Credentials", "true");
		OPTIONS_HEADERS.put("Access-Control-Allow-Headers", "Origin, X-Requested-With, Link, Content-Type, Cache-Control, Expires, X-Cache, X-HTTP-Method-Override, Accept");
		OPTIONS_HEADERS.put("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, PATCH, DELETE, TRACE, OPTIONS");
		OPTIONS_HEADERS.put("Access-Control-Expose-Headers", "Link");
	}

	public static Users users = null;

	@Override
	public void init() {

		users = new UsersFileImpl();
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if (request.getMethod().equals("PATCH")) {

			this.doPatch(request, response);
		} else {

			super.service(request, response);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String target = request.getParameter("url");
		User user = loadUser(request);
		if (user == null || user.getVerificationcode() != null) { this.error(request, response, HttpServletResponse.SC_UNAUTHORIZED, "User not found."); return; }

		HttpClient httpClient = MySSLSocketFactory.getHttpClient(request);
		HttpGet httpGet = new HttpGet(target);
		for (String copyHeader : COPY_HEADERS) if (request.getHeader(copyHeader) != null) httpGet.setHeader(copyHeader, request.getHeader(copyHeader));
		HttpResponse httpResponse = httpClient.execute(httpGet);

		log.info("PROXY GET " + target + " -> " + httpResponse.getStatusLine());
		HttpEntity entity = httpResponse.getEntity();

		response.setStatus(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
		for (Header header : httpResponse.getAllHeaders()) response.addHeader(header.getName(), header.getValue());
		for (Entry<String, String> header : GET_HEADERS.entrySet()) response.addHeader(header.getKey(), header.getValue());
		IOUtils.copy(entity.getContent(), response.getOutputStream());
		EntityUtils.consume(entity);
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String target = request.getParameter("url");
		User user = loadUser(request);
		if (user == null || user.getVerificationcode() != null) { this.error(request, response, HttpServletResponse.SC_UNAUTHORIZED, "User not found."); return; }

		HttpClient httpClient = MySSLSocketFactory.getHttpClient(request);
		HttpPut httpPut = new HttpPut(target);
		for (String copyHeader : COPY_HEADERS) if (request.getHeader(copyHeader) != null) httpPut.setHeader(copyHeader, request.getHeader(copyHeader));
		httpPut.setEntity(new InputStreamEntity(request.getInputStream()));
		HttpResponse httpResponse = httpClient.execute(httpPut);

		log.info("PROXY PUT " + target + " -> " + httpResponse.getStatusLine());
		HttpEntity entity = httpResponse.getEntity();

		response.setStatus(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
		for (Header header : httpResponse.getAllHeaders()) response.addHeader(header.getName(), header.getValue());
		for (Entry<String, String> header : PUT_HEADERS.entrySet()) response.addHeader(header.getKey(), header.getValue());
		IOUtils.copy(entity.getContent(), response.getOutputStream());
		EntityUtils.consume(entity);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String target = request.getParameter("url");
		User user = loadUser(request);
		if (user == null || user.getVerificationcode() != null) { this.error(request, response, HttpServletResponse.SC_UNAUTHORIZED, "User not found."); return; }

		HttpClient httpClient = MySSLSocketFactory.getHttpClient(request);
		HttpPost httpPost = new HttpPost(target);
		for (String copyHeader : COPY_HEADERS) if (request.getHeader(copyHeader) != null) httpPost.setHeader(copyHeader, request.getHeader(copyHeader));
		httpPost.setEntity(new InputStreamEntity(request.getInputStream()));
		HttpResponse httpResponse = httpClient.execute(httpPost);

		log.info("PROXY POST " + target + " -> " + httpResponse.getStatusLine());
		HttpEntity entity = httpResponse.getEntity();

		response.setStatus(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
		for (Header header : httpResponse.getAllHeaders()) response.addHeader(header.getName(), header.getValue());
		for (Entry<String, String> header : POST_HEADERS.entrySet()) response.addHeader(header.getKey(), header.getValue());
		IOUtils.copy(entity.getContent(), response.getOutputStream());
		EntityUtils.consume(entity);
	}

	protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String target = request.getParameter("url");
		User user = loadUser(request);
		if (user == null || user.getVerificationcode() != null) { this.error(request, response, HttpServletResponse.SC_UNAUTHORIZED, "User not found."); return; }

		HttpClient httpClient = MySSLSocketFactory.getHttpClient(request);
		HttpPatch httpPatch = new HttpPatch(target);
		for (String copyHeader : COPY_HEADERS) if (request.getHeader(copyHeader) != null) httpPatch.setHeader(copyHeader, request.getHeader(copyHeader));
		httpPatch.setEntity(new InputStreamEntity(request.getInputStream()));
		HttpResponse httpResponse = httpClient.execute(httpPatch);

		log.info("PROXY PATCH " + target + " -> " + httpResponse.getStatusLine());
		HttpEntity entity = httpResponse.getEntity();

		response.setStatus(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
		for (Header header : httpResponse.getAllHeaders()) response.addHeader(header.getName(), header.getValue());
		for (Entry<String, String> header : PATCH_HEADERS.entrySet()) response.addHeader(header.getKey(), header.getValue());
		IOUtils.copy(entity.getContent(), response.getOutputStream());
		EntityUtils.consume(entity);
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String target = request.getParameter("url");
		User user = loadUser(request);
		if (user == null || user.getVerificationcode() != null) { this.error(request, response, HttpServletResponse.SC_UNAUTHORIZED, "User not found."); return; }

		HttpClient httpClient = MySSLSocketFactory.getHttpClient(request);
		HttpDelete httpDelete = new HttpDelete(target);
		for (String copyHeader : COPY_HEADERS) if (request.getHeader(copyHeader) != null) httpDelete.setHeader(copyHeader, request.getHeader(copyHeader));
		HttpResponse httpResponse = httpClient.execute(httpDelete);

		log.info("PROXY DELETE " + target + " -> " + httpResponse.getStatusLine());
		HttpEntity entity = httpResponse.getEntity();

		response.setStatus(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
		for (Header header : httpResponse.getAllHeaders()) response.addHeader(header.getName(), header.getValue());
		for (Entry<String, String> header : DELETE_HEADERS.entrySet()) response.addHeader(header.getKey(), header.getValue());
		IOUtils.copy(entity.getContent(), response.getOutputStream());
		EntityUtils.consume(entity);
	}

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		super.doOptions(request, response);

		String target = request.getParameter("url");

		log.info("PROXY OPTIONS " + target);

		if (request.getHeader("Origin") != null) {

			response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		} else {

			response.setHeader("Access-Control-Allow-Origin", "*");
		}

		for (Entry<String, String> header : OPTIONS_HEADERS.entrySet()) response.addHeader(header.getKey(), header.getValue());
	}

	public static User loadUser(HttpServletRequest request) {

		HttpSession session = request.getSession(false);
		String username = session == null ? null : (String) session.getAttribute("username");
		log.debug("Username: " + username);
		User user = username == null ? null : WebIDProxyServlet.users.get(username);
		log.debug("User: " + user);

		return user;
	}
}
