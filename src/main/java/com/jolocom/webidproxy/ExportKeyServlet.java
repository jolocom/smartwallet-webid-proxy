package com.jolocom.webidproxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jolocom.webidproxy.users.User;

public class ExportKeyServlet extends BaseServlet {

	private static final long serialVersionUID = 1540763620371741373L;

	private static final Log log = LogFactory.getLog(ExportKeyServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		User user = WebIDProxyServlet.loadUser(request);
		if (user == null) { this.error(request, response, HttpServletResponse.SC_FORBIDDEN, "User not found."); return; }

		File keyfile = user == null ? null : new File("./users/" + user.getUsername() + ".p12");

		response.setStatus(HttpServletResponse.SC_OK);
		response.setHeader("Content-Type", "application/x-pkcs12");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + user.getUsername() + ".p12\"");
		IOUtils.copy(new FileInputStream(keyfile), response.getOutputStream());

		log.debug("Private key of user " + user.getUsername() + " successfully exported.");
	}
}
