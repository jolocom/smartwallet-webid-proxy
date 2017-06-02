package com.jolocom.webidproxy;

import java.io.IOException;
import java.security.KeyPair;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.mozilla.SignedPublicKeyAndChallenge;
import org.mindrot.jbcrypt.BCrypt;

import com.jolocom.webidproxy.email.RegisterEmail;
import com.jolocom.webidproxy.ssl.SSLGenerator;
import com.jolocom.webidproxy.users.User;
import com.jolocom.webidproxy.users.WebIDRegistration;
import com.jolocom.webidproxy.util.Util;

public class RegisterServlet extends BaseServlet {

	private static final long serialVersionUID = 3793048689633131588L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String name = request.getParameter("name");
		String email = request.getParameter("email");
		String privatekey = request.getParameter("privatekey");

		username = username.toLowerCase();

		// check username requirements

		if (! Util.isAlphaNumeric(username)) {

			this.error(request, response, HttpServletResponse.SC_BAD_REQUEST, "Username " + username + " is not alphanumeric.");
			return;
		}

		if (WebIDProxyServlet.users.exists(username)) {

			this.error(request, response, HttpServletResponse.SC_BAD_REQUEST, "User " + username + " exists already.");
			return;
		}

		// create WebID and KeyPair and SPKAC

		String webid;
		KeyPair keyPair;
		String spkac;

		try {

			webid = WebIDRegistration.webidForUsername(username);

			keyPair = SSLGenerator.parseKeyPair(privatekey);
			SignedPublicKeyAndChallenge signedPublicKeyAndChallenge = SSLGenerator.generateSignedPublicKeyAndChallenge(keyPair);

			spkac = Base64.encodeBase64String(signedPublicKeyAndChallenge.getEncoded());
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}

		// register user

		User user = WebIDProxyServlet.users.register(username, BCrypt.hashpw(password, BCrypt.gensalt()), name, webid, email, spkac, keyPair);
		request.getSession().setAttribute("username", username);
		request.getSession().setAttribute("HTTPCLIENT", null);

		// send e-mail

		RegisterEmail registerEmail = new RegisterEmail(user, email);

		try {

			registerEmail.send();
		} catch (MessagingException ex) {

			throw new ServletException("Cannot send e-mail: " + ex.getMessage(), ex);
		}

		// done

		String content = "{\"webid\":\"" + user.getWebid() + "\"}";

		this.success(request, response, content, "application/json");
	}
}
