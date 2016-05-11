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
package com.danubetech.webidproxy;

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

import com.danubetech.webidproxy.ssl.MySSLSocketFactory;
import com.danubetech.webidproxy.users.User;
import com.danubetech.webidproxy.users.Users;
import com.danubetech.webidproxy.users.UsersFileImpl;

public class WebIDProxyServlet extends HttpServlet {

	private static final long serialVersionUID = 3793048689633131588L;

	private static final Log log = LogFactory.getLog(WebIDProxyServlet.class);

	public static final String[] COPY_HEADERS = new String[] { "Accept" };

	public static Users users = null;

	@Override
	public void init() {

		users = new UsersFileImpl();
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String target = request.getRequestURI().substring("/proxy/".length());
		User user = loadUser(request);
		if (user == null) { response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not found."); return; }

		HttpClient httpClient = MySSLSocketFactory.getNewHttpClient(user);
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

		String target = request.getRequestURI().substring("/proxy/".length());
		User user = loadUser(request);
		if (user == null) { response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not found."); return; }

		HttpClient httpClient = MySSLSocketFactory.getNewHttpClient(user);
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

		String target = request.getRequestURI().substring("/proxy/".length());
		User user = loadUser(request);
		if (user == null) { response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not found."); return; }

		HttpClient httpClient = MySSLSocketFactory.getNewHttpClient(user);
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

		String target = request.getRequestURI().substring("/proxy/".length());
		User user = loadUser(request);
		if (user == null) { response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not found."); return; }

		HttpClient httpClient = MySSLSocketFactory.getNewHttpClient(user);
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

	private static User loadUser(HttpServletRequest request) {

		HttpSession session = request.getSession(false);
		String username = session == null ? null : (String) session.getAttribute("username");
		log.debug("Username: " + username);
		User user = username == null ? null : users.get(username);
		log.debug("User: " + user);

		return user;
	}
}
