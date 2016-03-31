/*
 *  WebID-TLS Proxy
 *  Copyright (C) 2016 Danube Tech GmbH
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.danubetech.webidproxy.users;

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

public class UsersFileImpl implements Users {

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

		try {

			WebIDRegistration.registerWebIDAccount(user);
		} catch (IOException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}

		try {

			save(user);
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}

		return user;
	}

	@Override
	public User get(String username) {

		try {

			return load(username);
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private static User load(String username) throws Exception {

		if (! DIR.exists()) return null;

		File file = new File(DIR, username);
		if (! file.exists()) return null;

		FileReader reader = new FileReader(file);
		Properties properties = new Properties();
		properties.load(reader);
		reader.close();

		return User.fromProperties(properties);
	}

	private static void save(User user) throws Exception {

		if (! DIR.exists()) DIR.mkdir();

		File file = new File(DIR, user.getUsername());

		FileWriter writer = new FileWriter(file);
		Properties properties = User.toProperties(user);
		properties.store(writer, user.getUsername());
		writer.close();

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
	}
}
