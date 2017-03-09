package com.jolocom.webidproxy.users;

import java.util.Properties;

public class User {

	public String username;
	public String password;
	public String name;
	public String webid;
	public String recoverycode;
	public String verificationcode;
	public String email;

	User(String username, String password, String name, String webid, String recoverycode, String verificationcode, String email) {

		this.username = username;
		this.password = password;
		this.name = name;
		this.webid = webid;
		this.recoverycode = recoverycode;
		this.verificationcode = verificationcode;
		this.email = email;
	}

	User(String username, String password, String name, String webid, String email) {

		this.username = username;
		this.password = password;
		this.name = name;
		this.webid = webid;
		this.recoverycode = null;
		this.verificationcode = null;
		this.email = email;
	}
	
	static User fromProperties(Properties properties) {

		return new User(
				properties.getProperty("username"),
				properties.getProperty("password"),
				properties.getProperty("name"),
				properties.getProperty("webid"),
				properties.getProperty("recoverycode"),
				properties.getProperty("verificationcode"),
				properties.getProperty("email"));
	}

	static Properties toProperties(User user) {

		Properties properties = new Properties();
		if (user.getUsername() != null) properties.setProperty("username", user.getUsername());
		if (user.getPassword() != null) properties.setProperty("password", user.getPassword());
		if (user.getName() != null) properties.setProperty("name", user.getName());
		if (user.getWebid() != null) properties.setProperty("webid", user.getWebid());
		if (user.getRecoverycode() != null) properties.setProperty("recoverycode", user.getRecoverycode());
		if (user.getVerificationcode() != null) properties.setProperty("verificationcode", user.getVerificationcode());
		if (user.getEmail() != null) properties.setProperty("email", user.getEmail());

		return properties;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWebid() {
		return webid;
	}

	public void setWebid(String webid) {
		this.webid = webid;
	}

	public String getRecoverycode() {
		return recoverycode;
	}

	public void setRecoverycode(String recoverycode) {
		this.recoverycode = recoverycode;
	}

	public String getVerificationcode() {
		return verificationcode;
	}

	public void setVerificationcode(String verificationcode) {
		this.verificationcode = verificationcode;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", name=" + name + ", webid=" + webid + "]";
	}
}
