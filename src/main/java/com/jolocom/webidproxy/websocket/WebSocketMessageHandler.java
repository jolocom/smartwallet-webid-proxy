package com.jolocom.webidproxy.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketMessageHandler implements javax.websocket.MessageHandler.Whole<String> {

	private static final Logger log = LoggerFactory.getLogger(WebSocketMessageHandler.class);

	private Session session;
	private String target;

	public WebSocketMessageHandler(Session session, String target) {

		this.session = session;
		this.target = target;
	}

	@Override
	public void onMessage(String string) {

		// read line

		BufferedReader bufferedReader = new BufferedReader(new StringReader(string));
		String line;

		try {

			line = bufferedReader.readLine();
		} catch (IOException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}

		log.info("Received line " + line + " from session " + this.session.getId());

		// first message? connect to Solid.

		if (line.startsWith("{")) {

			try {

				this.connectToSolid(line);
			} catch (Exception ex) {

				throw new RuntimeException("Cannot connect: " + ex.getMessage(), ex);
			}
			return;
		} 

		// send message

		try {

			this.sendToSolid(line);
		} catch (Exception ex) {

			throw new RuntimeException("Cannot send to XDI: " + ex.getMessage(), ex);
		}
	}

	private void connectToSolid(String line) throws GeneralSecurityException {

		// open connection to Solid

		// TODO
	}

	private void sendToSolid(String line) throws GeneralSecurityException {

		// send to solid

		// TODO
	}

	private void sendToClient(WebSocketMessageHandler fromWebSocketMessageHandler, String string) {

		// send to client

		this.session.getAsyncRemote().sendText(string);

		log.info("Sent string " + string + " to session " + this.session.getId());
	}

	/*
	 * Getters and setters
	 */

	public Session getSession() {

		return this.session;
	}

	public String getTarget() {

		return this.target;
	}
}
