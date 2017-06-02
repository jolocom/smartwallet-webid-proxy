package com.jolocom.webidproxy.users;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Properties;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UsersFileImpl implements Users {

	private static final Log log = LogFactory.getLog(UsersFileImpl.class);

	public static File DIR = new File("./users/");

	private static final int VERIFYCODE_LENGTH = 8;

	@Override
	public boolean exists(String username) {

		if (! DIR.exists()) return false;

		File file = new File(DIR, username);

		return file.exists();
	}

	@Override
	public User register(String username, String password, String name, String webid, String email, String spkac, KeyPair keyPair) {

		if (this.get(username) != null) throw new RuntimeException("User '" + username + "' exists already.");

		User user = new User(username, password, name, webid, email);

		// create verification code

		String verificationcode = generateVerificationcode();
		user.setVerificationcode(verificationcode);

		// save user locally

		try {

			saveUser(user);
		} catch (Exception ex) {

			deleteUserAndKey(user);
			throw new RuntimeException("Cannot register user: " + ex.getMessage(), ex);
		}

		// register user in Solid

		try {

			WebIDRegistration.registerWebIDAccount(user, email, spkac, keyPair);
		} catch (Exception ex) {

			deleteUserAndKey(user);
			throw new RuntimeException("Cannot register WebID: " + ex.getMessage(), ex);
		}

		// done

		return user;
	}

	@Override
	public User get(String username) {

		try {

			return loadUser(username);
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	@Override
	public void put(User user) {

		try {

			saveUser(user);
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public User loadUser(String username) throws Exception {

		if (! DIR.exists()) return null;

		File file = new File(DIR, username);
		if (! file.exists()) return null;

		FileReader reader = new FileReader(file);
		Properties properties = new Properties();
		properties.load(reader);
		reader.close();

		// done

		User user = User.fromProperties(properties);
		log.debug("Loaded user " + user);
		return user;
	}

	public static void saveUser(User user) throws IOException {

		if (! DIR.exists()) DIR.mkdir();

		File file = new File(DIR, user.getUsername());

		FileWriter writer = new FileWriter(file);
		Properties properties = User.toProperties(user);
		properties.store(writer, user.getUsername());
		writer.close();

		// done

		log.debug("Saved user " + user);
	}

	private static void deleteUserAndKey(User user) {

		File file = new File(DIR, user.getUsername());
		File ksfile = new File(DIR, user.getUsername() + ".p12");

		if (file.exists()) file.delete();
		if (ksfile.exists()) ksfile.delete();

		// done

		log.debug("Deleted user " + user);
	}

	private static String generateVerificationcode() {

		return RandomStringUtils.randomAlphanumeric(VERIFYCODE_LENGTH);
	}
}
