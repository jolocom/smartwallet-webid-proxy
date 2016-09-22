package com.jolocom.webidproxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

import com.jolocom.webidproxy.users.User;

public class LoginServlet extends BaseServlet {

	private static final long serialVersionUID = 3793048689633131588L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String username = request.getParameter("username");
		String password = request.getParameter("password");

		User user = WebIDProxyServlet.users.get(username);

		if (user == null || ! BCrypt.checkpw(password, user.getPassword())) {

			this.error(request, response, HttpServletResponse.SC_BAD_REQUEST, "User " + username + " cannot be authenticated.");
			return;
		}

		HttpSession session = request.getSession(true);
		session.setAttribute("username", username);
		session.setAttribute("HTTPCLIENT", null);

		String content = "{\"webid\":\"" + user.getWebid() + "\"}";

		this.success(request, response, content, "application/json");
	}
}
