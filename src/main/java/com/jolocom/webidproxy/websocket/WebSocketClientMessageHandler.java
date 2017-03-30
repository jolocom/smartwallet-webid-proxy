package com.jolocom.webidproxy.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolocom.webidproxy.websocket.WebSocketClient.ClientCallback;

public class WebSocketClientMessageHandler implements javax.websocket.MessageHandler.Whole<Reader> {

	private static final Logger log = LoggerFactory.getLogger(WebSocketClientMessageHandler.class);

	private Session session;

	public WebSocketClientMessageHandler(Session session) {

		this.session = session;
	}

	@Override
	public void onMessage(Reader reader) {

		if (log.isDebugEnabled()) log.debug("Incoming WebSocket message on session " + this.getSession().getId());

		// read properties

		WebSocketClient webSocketClient = (WebSocketClient) this.getSession().getUserProperties().get("WebSocketClient");

		// read message

		String message;

		try {

			message = new BufferedReader(reader).readLine();
		} catch (IOException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}

		// callbacks

		ClientCallback clientCallback = webSocketClient.getClientCallback();

		if (clientCallback != null) {

			if (log.isDebugEnabled()) log.debug("Calling WebSocketClient.onClientMessage() with " + message);
			clientCallback.onClientMessage(message);
		}
	}

	/*
	 * Getters and setters
	 */

	public Session getSession() {

		return this.session;
	}
}
