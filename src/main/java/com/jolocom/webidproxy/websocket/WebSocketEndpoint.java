package com.jolocom.webidproxy.websocket;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Decoder;
import javax.websocket.DeploymentException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import javax.websocket.Extension;
import javax.websocket.Session;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketEndpoint extends javax.websocket.Endpoint {

	private static final Logger log = LoggerFactory.getLogger(WebSocketEndpoint.class);

	private static final String PATH = "/websocket?url={target}";

	public static final List<WebSocketMessageHandler> WEBSOCKETMESSAGEHANDLERS = new ArrayList<WebSocketMessageHandler> ();

	public static void install(ServletContext servletContext) throws DeploymentException {

		// find server container

		ServerContainer serverContainer = (ServerContainer) servletContext.getAttribute("javax.websocket.server.ServerContainer");
		if (serverContainer == null) throw new DeploymentException("Cannot find ServerContainer");

		// init websocket endpoint

		List<String> subprotocols = Arrays.asList(new String[] { "solid-websocket" });
		List<Extension> extensions = null;
		List<Class<? extends Encoder>> encoders = null;
		List<Class<? extends Decoder>> decoders = null;

		ServerEndpointConfig.Configurator serverEndpointConfigConfigurator = new ServerEndpointConfig.Configurator() {

		};

		ServerEndpointConfig.Builder serverEndpointConfigBuilder = ServerEndpointConfig.Builder.create(
				WebSocketEndpoint.class, 
				PATH);

		serverEndpointConfigBuilder.subprotocols(subprotocols);
		serverEndpointConfigBuilder.extensions(extensions);
		serverEndpointConfigBuilder.encoders(encoders);
		serverEndpointConfigBuilder.decoders(decoders);
		serverEndpointConfigBuilder.configurator(serverEndpointConfigConfigurator);

		ServerEndpointConfig serverEndpointConfig = serverEndpointConfigBuilder.build();

		serverContainer.addEndpoint(serverEndpointConfig);

		// done

		log.info("Installed WebSocket endpoint at " + PATH + " with subprotocols " + subprotocols);
	}

	@Override
	public void onOpen(Session session, EndpointConfig endpointConfig) {

		// set timeout

		long oldMaxIdleTimeout = session.getMaxIdleTimeout();
		long newMaxIdleTimeout = 0;
		session.setMaxIdleTimeout(newMaxIdleTimeout);

		if (log.isDebugEnabled()) log.debug("Changed max idle timeout of session " + session.getId() + " from " + oldMaxIdleTimeout + " to " + newMaxIdleTimeout);

		// init message handler

		ServerEndpointConfig serverEndpointConfig = (ServerEndpointConfig) endpointConfig;

		try {

			// parse parameters

			String target = URLDecoder.decode(session.getPathParameters().get("target"), "UTF-8");

			// create message handler

			WebSocketMessageHandler webSocketMessageHandler = new WebSocketMessageHandler(session, target);

			session.addMessageHandler(webSocketMessageHandler);
			WEBSOCKETMESSAGEHANDLERS.add(webSocketMessageHandler);

			log.info("WebSocket session " + session.getId() + " opened (" + serverEndpointConfig.getPath() + ") with target " + target);
		} catch (Exception ex) {

			try {

				String reason = "Cannot add message handler: " + ex.getMessage();
				log.error(reason, ex);

				if (reason.length() > 120) reason = reason.substring(0, 120);

				session.close(new CloseReason(CloseCodes.PROTOCOL_ERROR, reason));
			} catch (IOException ex2) {

				throw new RuntimeException(ex2.getMessage(), ex2);
			}
		}
	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {

		// find message handler and connection

		WebSocketMessageHandler webSocketMessageHandler = (WebSocketMessageHandler) session.getMessageHandlers().iterator().next();

		// remove message handler

		session.removeMessageHandler(webSocketMessageHandler);
		WEBSOCKETMESSAGEHANDLERS.remove(webSocketMessageHandler);

		log.info("WebSocket session " + session.getId() + " closed.");
	}

	@Override
	public void onError(Session session, Throwable throwable) {

		log.error("WebSocket session " + session.getId() + " error: " + throwable.getMessage(), throwable);
	}
}