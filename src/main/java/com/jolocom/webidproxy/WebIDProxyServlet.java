package com.jolocom.webidproxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.EntityUtils;

import com.jolocom.webidproxy.ssl.MySSLSocketFactory;
import com.jolocom.webidproxy.users.User;
import com.jolocom.webidproxy.users.Users;
import com.jolocom.webidproxy.users.UsersFileImpl;

public class WebIDProxyServlet extends HttpServlet {

	private static final long serialVersionUID = 3793048689633131588L;

	private static final Log log = LogFactory.getLog(WebIDProxyServlet.class);

	public static final String[] COPY_HEADERS = new String[] { "Accept", "Content-Type", "Origin" };

	public static Users users = null;

	@Override
	public void init() {

		users = new UsersFileImpl();
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String target = request.getParameter("url");
		User user = loadUser(request);
		if (user == null) { response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not found."); return; }

		HttpClient httpClient = MySSLSocketFactory.getNewHttpClient(request, user);
		HttpPut httpPut = new HttpPut(target);
		for (String copyHeader : COPY_HEADERS) if (request.getHeader(copyHeader) != null) httpPut.setHeader(copyHeader, request.getHeader(copyHeader));
		httpPut.setEntity(new InputStreamEntity(request.getInputStream()));
		HttpResponse httpResponse = httpClient.execute(httpPut);

		log.info("PROXY PUT " + target + " -> " + httpResponse.getStatusLine());
		HttpEntity entity = httpResponse.getEntity();

		response.setStatus(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
		for (Header header : httpResponse.getAllHeaders()) response.addHeader(header.getName(), header.getValue());
		IOUtils.copy(entity.getContent(), response.getOutputStream());
		EntityUtils.consume(entity);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String target = request.getParameter("url");
		User user = loadUser(request);
		if (user == null) { response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not found."); return; }

		HttpClient httpClient = MySSLSocketFactory.getNewHttpClient(request, user);
		HttpPost httpPost = new HttpPost(target);
		for (String copyHeader : COPY_HEADERS) if (request.getHeader(copyHeader) != null) httpPost.setHeader(copyHeader, request.getHeader(copyHeader));
		httpPost.setEntity(new InputStreamEntity(request.getInputStream()));
		HttpResponse httpResponse = httpClient.execute(httpPost);

		log.info("PROXY POST " + target + " -> " + httpResponse.getStatusLine());
		HttpEntity entity = httpResponse.getEntity();

		response.setStatus(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
		for (Header header : httpResponse.getAllHeaders()) response.addHeader(header.getName(), header.getValue());
		IOUtils.copy(entity.getContent(), response.getOutputStream());
		EntityUtils.consume(entity);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String target = request.getParameter("url");
		User user = loadUser(request);
		if (user == null) { response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not found."); return; }

		HttpClient httpClient = MySSLSocketFactory.getNewHttpClient(request, user);
		HttpGet httpGet = new HttpGet(target);
		for (String copyHeader : COPY_HEADERS) if (request.getHeader(copyHeader) != null) httpGet.setHeader(copyHeader, request.getHeader(copyHeader));
		HttpResponse httpResponse = httpClient.execute(httpGet);

		log.info("PROXY GET " + target + " -> " + httpResponse.getStatusLine());
		HttpEntity entity = httpResponse.getEntity();

		response.setStatus(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
		for (Header header : httpResponse.getAllHeaders()) response.addHeader(header.getName(), header.getValue());
		IOUtils.copy(entity.getContent(), response.getOutputStream());
		EntityUtils.consume(entity);
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String target = request.getParameter("url");
		User user = loadUser(request);
		if (user == null) { response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not found."); return; }

		HttpClient httpClient = MySSLSocketFactory.getNewHttpClient(request, user);
		HttpDelete httpDelete = new HttpDelete(target);
		for (String copyHeader : COPY_HEADERS) if (request.getHeader(copyHeader) != null) httpDelete.setHeader(copyHeader, request.getHeader(copyHeader));
		HttpResponse httpResponse = httpClient.execute(httpDelete);

		log.info("PROXY DELETE " + target + " -> " + httpResponse.getStatusLine());
		HttpEntity entity = httpResponse.getEntity();

		response.setStatus(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
		for (Header header : httpResponse.getAllHeaders()) response.addHeader(header.getName(), header.getValue());
		IOUtils.copy(entity.getContent(), response.getOutputStream());
		EntityUtils.consume(entity);
	}

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		super.doOptions(request, response);

		String target = request.getParameter("url");
		User user = loadUser(request);
		if (user == null) { response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not found."); return; }

		log.info("PROXY OPTIONS " + target);

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Cache-Control, Expires, X-Cache, X-HTTP-Method-Override, Accept");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS");
	}

	private static User loadUser(HttpServletRequest request) {

		HttpSession session = request.getSession(false);
		String username = session == null ? null : (String) session.getAttribute("username");
		log.debug("Username: " + username);
		User user = username == null ? null : users.get(username);
		log.debug("User: " + user);

		return user;
	}
}
