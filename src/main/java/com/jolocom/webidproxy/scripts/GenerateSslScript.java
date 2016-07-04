package com.jolocom.webidproxy.scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jolocom.webidproxy.config.Config;

public class GenerateSslScript {

	private static final Log log = LogFactory.getLog(GenerateSslScript.class);

	public static final String SCRIPT_PATH = "./scripts/nginx-ssl/generate-ssl-for-webid.sh";
	public static final String CERTBOT_PATH = "/root";
	public static final String WEBROOT_PATH = "/usr/share/nginx/html";

	public static void execute(String username) throws IOException, InterruptedException {

		final String webIdHostWithoutPort = Config.webidHost().substring(0,  Config.webidHost().indexOf(':')); 

		StringBuffer line = new StringBuffer();
		line.append(SCRIPT_PATH);
		line.append(" -u " + username);
		line.append(" -d " + webIdHostWithoutPort);
		line.append(" -c " + CERTBOT_PATH);
		line.append(" -w " + WEBROOT_PATH);
		line.append(" -q");

		if (log.isDebugEnabled()) log.debug("Executing script " + line.toString());

		Process process = Runtime.getRuntime().exec(line.toString(), null);

		int exitValue = process.waitFor();
		if (log.isDebugEnabled()) log.debug("Script exit value " + exitValue);

		String output = readStream(process.getInputStream());
		String error = readStream(process.getErrorStream());
		if (log.isDebugEnabled()) log.debug("Script standard output " + output);
		if (log.isDebugEnabled()) log.debug("Script standard error " + error);

		if (exitValue != 0 || error != null) throw new RuntimeException("Script error: " + exitValue + " (" + output + ") (" + error + ")");
	}

	private static String readStream(InputStream stream) throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		StringBuffer buffer = new StringBuffer();
		String line;

		while ((line = reader.readLine()) != null) buffer.append(line + "\n");

		return buffer.length() == 0 ? null : buffer.toString();
	}
}
