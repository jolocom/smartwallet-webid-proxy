package com.jolocom.webidproxy.websocket;

import java.net.URI;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketClient {

	private static final Logger log = LoggerFactory.getLogger(WebSocketClient.class);

	private URI webSocketEndpointUri;

	private Session session;
	private ClientCallback clientCallback;

	public WebSocketClient(URI webSocketEndpointUri) {

		super();

		this.webSocketEndpointUri = webSocketEndpointUri;

		this.session = null;
		this.clientCallback = null;
	}

	public WebSocketClient(String webSocketEndpointUri) {

		this(URI.create(webSocketEndpointUri));
	}

	public WebSocketClient() {

		this((URI) null);
	}

	public void send(String message) throws RuntimeException {

		// connect

		Session session = null;

		try {

			session = this.connect();
		} catch (Exception ex) {

			this.disconnect(new CloseReason(CloseCodes.PROTOCOL_ERROR, "Cannot open WebSocket connection: " + ex.getMessage()));

			throw new RuntimeException("Cannot open WebSocket connection: " + ex.getMessage(), ex);
		}

		// send the message

		if (log.isDebugEnabled()) log.debug("Message: " + message);

		try {

			Async async = session.getAsyncRemote();

			async.sendText(message);
		} catch (Exception ex) {

			this.disconnect(new CloseReason(CloseCodes.PROTOCOL_ERROR, "Cannot send message envelope: " + ex.getMessage()));

			throw new RuntimeException("Cannot send message envelope: " + ex.getMessage(), ex);
		}
	}

	public void close() {

		this.disconnect(new CloseReason(CloseCodes.NORMAL_CLOSURE, "Bye."));
	}

	private Session connect() throws Exception {

		if (this.getSession() != null) return this.getSession();

		if (this.getWebSocketEndpointUri() == null) throw new RuntimeException("No URL to connect to.");

		// connect

		if (log.isDebugEnabled()) log.debug("Connecting to " + this.getWebSocketEndpointUri());

		Session session = WebSocketClientEndpoint.connect(this, this.getWebSocketEndpointUri()).getSession();

		// done

		if (log.isDebugEnabled()) log.debug("Connected successfully.");

		this.setSession(session);
		return session;
	}

	private void disconnect(CloseReason closeReason) {

		try {

			if (this.getSession() != null) {

				if (this.getSession().isOpen()) {

					this.getSession().close(closeReason);
				}
			}
		} catch (Exception ex) {

			log.error("Cannot disconnect: " + ex.getMessage(), ex);
		} finally {

			this.setSession(null);
		}

		if (log.isDebugEnabled()) log.debug("Disconnected successfully.");
	}

	/*
	 * Getters and setters
	 */

	public Session getSession() {

		return this.session;
	}

	public void setSession(Session session) {

		this.session = session;
	}

	public URI getWebSocketEndpointUri() {

		return this.webSocketEndpointUri;
	}

	public void setWebSocketEndpointUri(URI webSocketEndpointUri) {

		this.webSocketEndpointUri = webSocketEndpointUri;
	}

	public ClientCallback getClientCallback() {

		return this.clientCallback;
	}

	public void setClientCallback(ClientCallback clientCallback) {

		this.clientCallback = clientCallback;
	}

	/*
	 * Object methods
	 */

	@Override
	public String toString() {

		return this.getWebSocketEndpointUri().toString();
	}

	/*
	 * Helper classes
	 */

	public static interface ClientCallback {

		public void onClientMessage(String message);
	}
}
