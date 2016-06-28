package com.jolocom.webidproxy.logger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class HttpServletResponseCopier extends HttpServletResponseWrapper {

	private ServletOutputStream outputStream;
	private PrintWriter writer;
	private ServletOutputStreamCopier copier;
	Map<String, List<String>> headers = new HashMap<String, List<String>> ();

	public HttpServletResponseCopier(HttpServletResponse response) throws IOException {

		super(response);
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {

		if (writer != null) {

			throw new IllegalStateException("getWriter() has already been called on this response.");
		}

		if (outputStream == null) {

			outputStream = getResponse().getOutputStream();
			copier = new ServletOutputStreamCopier(outputStream);
		}

		return copier;
	}

	@Override
	public PrintWriter getWriter() throws IOException {

		if (outputStream != null) {

			throw new IllegalStateException("getOutputStream() has already been called on this response.");
		}

		if (writer == null) {

			copier = new ServletOutputStreamCopier(getResponse().getOutputStream());
			writer = new PrintWriter(new OutputStreamWriter(copier, getResponse().getCharacterEncoding()), true);
		}

		return writer;
	}

	@Override
	public void flushBuffer() throws IOException {

		if (writer != null) {

			writer.flush();
		} else if (outputStream != null) {

			copier.flush();
		}
	}

	@Override
	public void setHeader(String name, String value) {

		List<String> values = new ArrayList<String>();
		values.add(value);

		headers.put(name, values);

		super.setHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value) {

		List<String> values = headers.get(name);

		if (values == null) {

			values = new ArrayList<String>(); 
			headers.put(name, values);
		}

		values.add(value);

		super.addHeader(name, value);
	}

	public byte[] getCopy() {

		if (copier != null) {

			return copier.getCopy();
		} else {

			return new byte[0];
		}
	}

}