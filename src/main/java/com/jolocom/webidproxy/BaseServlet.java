package com.jolocom.webidproxy;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseServlet extends HttpServlet {

	private static final long serialVersionUID = -3822229595411460387L;

	private static final Log log = LogFactory.getLog(BaseServlet.class);

	protected void success(HttpServletRequest request, HttpServletResponse response, String content, String contentType) throws IOException {

		log.debug("SUCCESS: " + this.getClass().getSimpleName());

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

	protected void error(HttpServletRequest request, HttpServletResponse response, int sc, String msg) throws IOException {

		log.warn("ERROR: " + this.getClass().getSimpleName() + ": " + msg);

		response.setStatus(sc, msg);

		if (request.getHeader("Origin") != null) {

			response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		} else {

			response.setHeader("Access-Control-Allow-Origin", "*");
		}

		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Link");

		response.getWriter().print(msg);
		response.getWriter().flush();
	}
}
