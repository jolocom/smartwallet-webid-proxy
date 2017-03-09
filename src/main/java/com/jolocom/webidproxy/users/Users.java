package com.jolocom.webidproxy.users;

import java.security.KeyPair;

public interface Users {

	public boolean exists(String username);
	public User register(String username, String password, String name, String webid, String email, String spkac, KeyPair keyPair);
	public User get(String username);
	public void put(User user);
}
