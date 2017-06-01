package com.jolocom.webidproxy.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.security.GeneralSecurityException;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketMessageHandler implements javax.websocket.MessageHandler.Whole<String>, WebSocketClient.ClientCallback {

	private static final Logger log = LoggerFactory.getLogger(WebSocketMessageHandler.class);

	private Session session;
	private String target;
	private WebSocketClient client;

	public WebSocketMessageHandler(Session session, String target) {

		this.session = session;
		this.target = target;
		this.client = null;
	}

	@Override
	public void onMessage(String string) {

		// read line

		BufferedReader bufferedReader = new BufferedReader(new StringReader(string));
		String message;

		try {

			message = bufferedReader.readLine();
		} catch (IOException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}

		log.info("Received message " + message + " from session " + this.session.getId());

		// first message? connect to Solid.

		try {

			if (this.client == null) this.connectToSolid();
		} catch (Exception ex) {

			throw new RuntimeException("Cannot connect: " + ex.getMessage(), ex);
		}

		// send message

		try {

			this.sendToSolid(message);
		} catch (Exception ex) {

			throw new RuntimeException("Cannot send to Solid: " + ex.getMessage(), ex);
		}
	}

	private void connectToSolid() throws GeneralSecurityException {

		// open connection to Solid

		this.client = new WebSocketClient();
		this.client.setWebSocketEndpointUri(URI.create(this.target));

		this.client.setClientCallback(this);
	}

	private void sendToSolid(String message) throws GeneralSecurityException {

		// send to solid

		this.client.send(message);
	}

	private void sendToClient(String message) {

		// send to client

		this.session.getAsyncRemote().sendText(message);

		log.info("Sent message " + message + " to session " + this.session.getId());
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

	@Override
	public void onClientMessage(String message) {

		log.debug("Received client message: " + message);

		this.sendToClient(message);
	}
}
