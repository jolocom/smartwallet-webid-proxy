package com.jolocom.webidproxy.users;

import java.security.cert.X509Certificate;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;

import com.jolocom.webidproxy.config.Config;
import com.jolocom.webidproxy.ssl.SSLGenerator;

public class User {

	public static final int keyLength = 2048;
	public static final int certDays = 5000;

	public String username;
	public String password;
	public String name;
	public String email;
	public String webid;
	public String spkac;
	public String privatekey;
	public String certificate;

	User(String username, String password, String name, String email, String webid, String spkac, String privatekey, String certificate) {

		this.username = username;
		this.password = password;
		this.name = name;
		this.email = email;
		this.webid = webid;
		this.spkac = spkac;
		this.privatekey = privatekey;
		this.certificate = certificate;
	}

	User(String username, String password, String name, String email) {

		this.username = username;
		this.password = password;
		this.name = name;
		this.email = email;

		if (Config.vhosts()) {

			this.webid = "https://" + username + "." + Config.webidHost() + "/profile/card#me";
		} else {

			this.webid = "https://" + Config.webidHost() + "/" + username + "/profile/card#me";
		}

		try {

			oracle.security.crypto.core.KeyPair keyPair = SSLGenerator.generateKeyPair(keyLength);
			oracle.security.crypto.cert.SPKAC spkac = SSLGenerator.generateSPKAC(keyPair);
			X509Certificate cert = SSLGenerator.generateCertificate("CN=" + "WebID" + ", O=WebID, ST=Some-State, C=US", this.webid, SSLGenerator.convertKeyPair(keyPair), certDays, "X509");

			this.spkac = spkac.toBase64();
			this.privatekey = Base64.encodeBase64String(keyPair.getPrivate().getEncoded());
			this.certificate = Base64.encodeBase64String(cert.getEncoded());
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	static User fromProperties(Properties properties) {

		return new User(
				properties.getProperty("username"),
				properties.getProperty("password"),
				properties.getProperty("name"),
				properties.getProperty("email"),
				properties.getProperty("webid"),
				properties.getProperty("spkac"),
				properties.getProperty("privatekey"),
				properties.getProperty("certificate"));
	}

	static Properties toProperties(User user) {

		Properties properties = new Properties();
		if (user.getUsername() != null) properties.setProperty("username", user.getUsername());
		if (user.getPassword() != null) properties.setProperty("password", user.getPassword());
		if (user.getName() != null) properties.setProperty("name", user.getName());
		if (user.getEmail() != null) properties.setProperty("email", user.getEmail());
		if (user.getWebid() != null) properties.setProperty("webid", user.getWebid());
		if (user.getSpkac() != null) properties.setProperty("spkac", user.getSpkac());
		if (user.getPrivatekey() != null) properties.setProperty("privatekey", user.getPrivatekey());
		if (user.getCertificate() != null) properties.setProperty("certificate", user.getCertificate());

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getWebid() {
		return webid;
	}

	public void setWebid(String webid) {
		this.webid = webid;
	}

	public String getSpkac() {
		return spkac;
	}

	public void setSpkac(String spkac) {
		this.spkac = spkac;
	}

	public String getPrivatekey() {
		return privatekey;
	}

	public void setPrivatekey(String privatekey) {
		this.privatekey = privatekey;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", name=" + name + ", email=" + email + ", webid=" + webid + "]";
	}
}
