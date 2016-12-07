package com.jolocom.webidproxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jolocom.webidproxy.users.User;

public class VerifyEmailServlet extends BaseServlet {

	private static final long serialVersionUID = -3438786731246079920L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String username = request.getParameter("username");
		String code = request.getParameter("code");

		User user = WebIDProxyServlet.users.get(username);

		if (user == null || code == null || ! code.equals(user.getVerificationcode())) {

			this.error(request, response, HttpServletResponse.SC_BAD_REQUEST, "User " + username + " cannot verify e-mail.");
			return;
		}
		
		user.setVerificationcode(null);
		WebIDProxyServlet.users.put(user);

		String content = "{\"email\":\"" + user.getEmail() + "\"}";
		this.success(request, response, content, "application/json");
	}
}
