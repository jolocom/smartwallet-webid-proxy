package com.danubetech.webidproxy.users;

public interface Users {

	public boolean exists(String username);
	public User register(String username, String password, String name, String email);
	public User get(String username);
}
