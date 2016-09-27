package com.jolocom.webidproxy.email;

import com.jolocom.webidproxy.users.User;

public class VerifyRegistrationEmail extends Email {

	public VerifyRegistrationEmail(User user) {

		super(user, "/email-forgot-password.vm");
	}
}
