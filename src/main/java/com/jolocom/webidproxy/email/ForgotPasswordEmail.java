package com.jolocom.webidproxy.email;

import com.jolocom.webidproxy.users.User;

public class ForgotPasswordEmail extends Email {

	public ForgotPasswordEmail(User user) {

		super(user, "/email-forgot-password.vm");
	}
}
