package com.jolocom.webidproxy.email;

import com.jolocom.webidproxy.users.User;

public class RegisterEmail extends Email {

	public RegisterEmail(User user) {

		super(user, "/email-register.vm");
	}
}
