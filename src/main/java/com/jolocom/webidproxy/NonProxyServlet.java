package com.jolocom.webidproxy;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class NonProxyServlet extends HttpServlet {

	private static final long serialVersionUID = -3822229595411460387L;

	protected void success(HttpServletRequest request, HttpServletResponse response, String content, String contentType) throws IOException {

		response.setStatus(HttpServletResponse.SC_OK);

		if (request.getHeader("Origin") != null) {

			response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		} else {

			response.setHeader("Access-Control-Allow-Origin", "*");
		}

		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Link");

		if (contentType != null) {

			response.setContentType(contentType);
		}

		if (content != null) {

			response.getWriter().print(content);
			response.getWriter().flush();
		}
	}
}
