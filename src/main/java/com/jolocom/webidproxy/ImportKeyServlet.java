package com.jolocom.webidproxy;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jolocom.webidproxy.users.User;

public class ImportKeyServlet extends BaseServlet {

	private static final long serialVersionUID = -2703902409904957575L;

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

		WebIDProxyServlet.users.put(user);

		String content = "{}";

		this.success(request, response, content, "application/json");
	}
}
