package com.jolocom.webidproxy;

import java.io.IOException;
import java.security.KeyPair;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

import com.jolocom.webidproxy.ssl.MySSLSocketFactory;
import com.jolocom.webidproxy.ssl.SSLGenerator;
import com.jolocom.webidproxy.users.User;

public class LoginServlet extends BaseServlet {

	private static final long serialVersionUID = 3793048689633131588L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String privatekey = request.getParameter("privatekey");

		User user = WebIDProxyServlet.users.get(username);

		if (user == null || ! BCrypt.checkpw(password, user.getPassword())) {

			this.error(request, response, HttpServletResponse.SC_BAD_REQUEST, "User " + username + " cannot be authenticated.");
			return;
		}

		KeyPair keyPair = SSLGenerator.parseKeyPair(privatekey);
		MySSLSocketFactory.createHttpClient(request, user, keyPair);

		HttpSession session = request.getSession(true);
		session.setMaxInactiveInterval(2592000);
		session.setAttribute("username", username);

		String content = "{\"webid\":\"" + user.getWebid() + "\"}";

		this.success(request, response, content, "application/json");
	}
}
