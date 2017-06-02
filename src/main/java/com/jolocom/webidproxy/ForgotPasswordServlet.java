package com.jolocom.webidproxy;

import java.io.IOException;
import java.security.KeyPair;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;

import com.jolocom.webidproxy.email.ForgotPasswordEmail;
import com.jolocom.webidproxy.users.User;
import com.jolocom.webidproxy.users.WebIDRegistration;

public class ForgotPasswordServlet extends BaseServlet {

	private static final long serialVersionUID = 3793048689633131588L;

	private static final Log log = LogFactory.getLog(ForgotPasswordServlet.class);

	private static final int RECOVERYCODE_LENGTH = 8;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String username = request.getParameter("username");

		User user = WebIDProxyServlet.users.get(username);

		if (user == null) {

			this.error(request, response, HttpServletResponse.SC_BAD_REQUEST, "User " + username + " not found.");
			return;
		}

		// create recovery code

		String recoverycode = generateRecoverycode();
		user.setRecoverycode(recoverycode);
		WebIDProxyServlet.users.put(user);

		log.debug("User " + username + " forgot password, created recovery code.");

		// find out user's email from LDP

		String to;

		try {

			to = WebIDRegistration.retrieveUserEmail(request, user, (KeyPair) null);
		} catch (DataRetrievalException ex) {

			throw new ServletException("Cannot retrieve e-mail of user " + username + ": " + ex.getMessage(), ex);
		}

		if (to == null) throw new ServletException("No e-mail registered for user " + username);

		log.debug("Found " + username + " email: " + to);

		// send e-mail

		ForgotPasswordEmail email = new ForgotPasswordEmail(user, to);

		try {

			email.send();
		} catch (MessagingException ex) {

			throw new ServletException("Cannot send e-mail: " + ex.getMessage(), ex);
		}

		// done

		String content = "{}";

		this.success(request, response, content, "application/json");
	}

	private static String generateRecoverycode() {

		return RandomStringUtils.randomAlphanumeric(RECOVERYCODE_LENGTH);
	}
}
