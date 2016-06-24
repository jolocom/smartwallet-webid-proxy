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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RegisterServlet extends HttpServlet {

	private static final long serialVersionUID = 3793048689633131588L;

	private static final Log log = LogFactory.getLog(RegisterServlet.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String name = request.getParameter("name");
		String email = request.getParameter("email");

		if (WebIDProxyServlet.users.exists(username)) {

			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User " + username + " exists already.");
			log.debug("User " + username + " exists already.");
			return;
		}

/*		try {

			KeyStore ks = KeyStore.getInstance(CLIENT_KEYSTORE_TYPE);
			ks.load(new FileInputStream(CLIENT_KEYSTORE_PATH), CLIENT_KEYSTORE_PASS.toCharArray());
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex)
		}*/

		WebIDProxyServlet.users.register(username, password, name, email);
		request.getSession().setAttribute("username", username);
		request.getSession().setAttribute("HTTPCLIENT", null);
		log.debug("User " + username + " successfully registered and logged in.");
	}
}
