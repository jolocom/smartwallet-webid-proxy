package com.jolocom.webidproxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mindrot.jbcrypt.BCrypt;

import com.jolocom.webidproxy.users.User;
import com.jolocom.webidproxy.util.Util;

public class RegisterServlet extends BaseServlet {

	private static final long serialVersionUID = 3793048689633131588L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String name = request.getParameter("name");
		String email = request.getParameter("email");

		username = username.toLowerCase();

		if (! Util.isAlphaNumeric(username)) {

			this.error(request, response, HttpServletResponse.SC_BAD_REQUEST, "Username " + username + " is not alphanumeric.");
			return;
		}

		if (WebIDProxyServlet.users.exists(username)) {

			this.error(request, response, HttpServletResponse.SC_BAD_REQUEST, "User " + username + " exists already.");
			return;
		}

		/*		try {

			KeyStore ks = KeyStore.getInstance(CLIENT_KEYSTORE_TYPE);
			ks.load(new FileInputStream(CLIENT_KEYSTORE_PATH), CLIENT_KEYSTORE_PASS.toCharArray());
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex)
		}*/

		User user = WebIDProxyServlet.users.register(username, BCrypt.hashpw(password,BCrypt.gensalt()), name, email);
		request.getSession().setAttribute("username", username);
		request.getSession().setAttribute("HTTPCLIENT", null);

		String content = "{\"webid\":\"" + user.getWebid() + "\"}";

		this.success(request, response, content, "application/json");
	}
}
