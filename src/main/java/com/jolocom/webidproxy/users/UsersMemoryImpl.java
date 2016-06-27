package com.jolocom.webidproxy.users;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UsersMemoryImpl implements Users {

	private Map<String, User> users = new HashMap<String, User> ();

	@Override
	public boolean exists(String username) {

		return this.users.containsKey(username);
	}

	@Override
	public User register(String username, String password, String name, String email) {

		if (this.get(username) != null) throw new RuntimeException("User '" + username + "' exists already.");

		User user = new User(username, password, name, email);

		try {

			WebIDRegistration.registerWebIDAccount(user);
		} catch (IOException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}

		this.users.put(username, user);
		
		return user;
	}

	@Override
	public User get(String username) {

		return this.users.get(username);
	}
}
