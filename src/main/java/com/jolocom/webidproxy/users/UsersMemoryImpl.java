package com.jolocom.webidproxy.users;

import java.io.IOException;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

public class UsersMemoryImpl implements Users {

	private Map<String, User> users = new HashMap<String, User> ();

	@Override
	public boolean exists(String username) {

		return this.users.containsKey(username);
	}

	@Override
	public User register(String username, String password, String name, String webid, String email, String spkac, KeyPair keyPair) {

		if (this.get(username) != null) throw new RuntimeException("User '" + username + "' exists already.");

		// save user locally

		User user = new User(username, password, name, webid, email);

		this.users.put(username, user);

		// register user in Solid

		try {

			WebIDRegistration.registerWebIDAccount(user, email, spkac, keyPair);
		} catch (IOException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}

		// done

		return user;
	}

	@Override
	public User get(String username) {

		return this.users.get(username);
	}

	public void put(User user) {

		this.users.put(user.getUsername(), user);
	}
}
