package com.jolocom.webidproxy.logger;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResponseLoggerFilter implements Filter {

	private static final Log log = LogFactory.getLog(ResponseLoggerFilter.class);

	@Override
	public void init(FilterConfig config) throws ServletException {

	}

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {

		this.logRequest((HttpServletRequest) request);

		HttpServletResponseCopier responseCopier = new HttpServletResponseCopier((HttpServletResponse) response);

		try {

			chain.doFilter(request, responseCopier);
			responseCopier.flushBuffer();
		} finally {

			for (Entry<String, List<String>> headerList : responseCopier.headers.entrySet()) {

				String headerName = headerList.getKey();

				for (String headerValue : headerList.getValue()) {

					log.debug("PROXY>>CLIENT HEADER: " + headerName + " -> " + headerValue);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void logRequest(HttpServletRequest request) {

		log.debug("CLIENT>>PROXY REQUEST: " + request.getMethod() + " " + request.getRequestURI());

		for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements(); ) {

			String headerName = headerNames.nextElement();

			for (Enumeration<String> headerValues = request.getHeaders(headerName); headerValues.hasMoreElements(); ) {

				String headerValue = headerValues.nextElement();

				log.debug("CLIENT>>PROXY HEADER: " + headerName + " -> " + headerValue);
			}
		}
	}
}