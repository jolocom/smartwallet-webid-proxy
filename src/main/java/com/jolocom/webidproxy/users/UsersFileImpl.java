package com.jolocom.webidproxy.users;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UsersFileImpl implements Users {

	private static final Log log = LogFactory.getLog(UsersFileImpl.class);

	public static File DIR = new File("./users/");

	private static final String CLIENT_KEYSTORE_TYPE = "PKCS12";
	private static final String CLIENT_KEYSTORE_PASS = "changeit";

	@Override
	public boolean exists(String username) {

		if (! DIR.exists()) return false;

		File file = new File(DIR, username);

		return file.exists();
	}

	@Override
	public User register(String username, String password, String name, String email) {

		if (this.get(username) != null) throw new RuntimeException("User '" + username + "' exists already.");

		User user = new User(username, password, name, email);

		// save user locally

		try {

			saveUser(user);
			saveKey(user);
		} catch (Exception ex) {

			deleteUserAndKey(user);
			throw new RuntimeException("Cannot register user: " + ex.getMessage(), ex);
		}

		// register user in Solid

		try {

			WebIDRegistration.registerWebIDAccount(user);
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

	public static void saveKey(User user) throws Exception {

		if (! DIR.exists()) DIR.mkdir();

		// key store

		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(user.getPrivatekey().getBytes(Charset.forName("UTF-8"))));
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");

		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		X509Certificate certificate = (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(Base64.decodeBase64(user.getCertificate())));

		final PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

		// key manager

		File ksfile = new File(DIR, user.getUsername() + ".p12");

		KeyStore ks = KeyStore.getInstance(CLIENT_KEYSTORE_TYPE);
		ks.load(null, CLIENT_KEYSTORE_PASS.toCharArray());
		ks.setKeyEntry(user.getUsername(), privateKey, CLIENT_KEYSTORE_PASS.toCharArray(), new Certificate[] { certificate });
		ks.store(new FileOutputStream(ksfile), CLIENT_KEYSTORE_PASS.toCharArray());

		// done

		log.debug("Saved key " + user);
	}

	private static void deleteUserAndKey(User user) {

		File file = new File(DIR, user.getUsername());
		File ksfile = new File(DIR, user.getUsername() + ".p12");

		if (file.exists()) file.delete();
		if (ksfile.exists()) ksfile.delete();

		// done

		log.debug("Deleted user " + user);
	}
}
