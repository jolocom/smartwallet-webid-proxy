package com.jolocom.webidproxy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jolocom.webidproxy.ssl.SSLGenerator;
import com.jolocom.webidproxy.users.User;
import com.jolocom.webidproxy.users.UsersFileImpl;
import com.jolocom.webidproxy.users.WebIDRegistration;

public class ImportKeyServlet extends BaseServlet {

	private static final long serialVersionUID = -2703902409904957575L;

	private static final String CLIENT_KEYSTORE_PASS = "changeit";

	private static final Log log = LogFactory.getLog(ImportKeyServlet.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		List<FileItem> fileItems;

		try {

			fileItems = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
			log.debug("Received file items: " + fileItems);
		} catch (FileUploadException ex) {

			throw new IOException(ex.getMessage(), ex);
		}

		User user = WebIDProxyServlet.loadUser(request);

		if (user == null || fileItems == null || fileItems.size() != 1) {

			this.error(request, response, HttpServletResponse.SC_BAD_REQUEST, "User cannot import key.");
			return;
		}

		String privateKey;
		String certificate;
		String spkac;

		try {

			KeyStore store = KeyStore.getInstance("PKCS12");
			store.load(fileItems.get(0).getInputStream(), CLIENT_KEYSTORE_PASS.toCharArray());
			PrivateKeyEntry privateKeyEntry = (PrivateKeyEntry) store.getEntry(user.getUsername(), new PasswordProtection(CLIENT_KEYSTORE_PASS.toCharArray()));
			if (privateKeyEntry == null) { this.error(request, response, HttpServletResponse.SC_BAD_REQUEST, "Key entry not found in uploaded file"); return; }
			privateKey = Base64.encodeBase64String(privateKeyEntry.getPrivateKey().getEncoded());
			certificate = Base64.encodeBase64String(privateKeyEntry.getCertificate().getEncoded());
			spkac = Base64.encodeBase64String(SSLGenerator.generateSignedPublicKeyAndChallenge(new KeyPair(privateKeyEntry.getCertificate().getPublicKey(), privateKeyEntry.getPrivateKey())).getEncoded());
		} catch (GeneralSecurityException ex) {

			throw new IOException(ex.getMessage(), ex);
		}

		user.setPrivatekey(privateKey);
		user.setCertificate(certificate);
		user.setSpkac(spkac);
		WebIDProxyServlet.users.put(user);

		WebIDRegistration.newWebIDCert(request, user);

		Files.copy(fileItems.get(0).getInputStream(), new File(UsersFileImpl.DIR, user.getUsername() + ".p12").toPath(), StandardCopyOption.REPLACE_EXISTING);
		fileItems.get(0).delete();

		String content = "{}";

		this.success(request, response, content, "application/json");
	}
}
