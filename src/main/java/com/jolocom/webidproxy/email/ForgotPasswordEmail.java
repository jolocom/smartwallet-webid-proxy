package com.jolocom.webidproxy.email;

import com.jolocom.webidproxy.users.User;

public class ForgotPasswordEmail extends Email {

	public ForgotPasswordEmail(User user, String to) {

		super(user, to, "/email-forgot-password.vm");
	}
}
