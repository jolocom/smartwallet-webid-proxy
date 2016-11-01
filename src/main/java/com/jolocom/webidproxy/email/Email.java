package com.jolocom.webidproxy.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.jolocom.webidproxy.users.User;

public abstract class Email {

	private static final Log log = LogFactory.getLog(Email.class);

	private User user;
	private String host;
	private String to;
	private String from;
	private String subject;
	private String template;

	public Email(User user, String resource) {

		this.user = user;
		this.host = "localhost";
		this.to = user.getEmail();

		this.readResource(resource);
	}

	public void readResource(String resource) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(ForgotPasswordEmail.class.getResourceAsStream(resource), StandardCharsets.UTF_8));
		StringBuffer template = new StringBuffer();

		try {

			this.setFrom(reader.readLine());
			this.setSubject(reader.readLine());

			String line;
			while (((line = reader.readLine()) != null)) template.append(line);
			this.setTemplate(template.toString());
			reader.close();
		} catch (IOException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public void send() throws MessagingException {

		// set host

		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", this.getHost());

		// check if we have everything

		if (this.getFrom() == null) throw new MessagingException("No e-mail sender address available.");
		if (this.getTo() == null) throw new MessagingException("No e-mail recipient address available.");
		if (this.getSubject() == null) throw new MessagingException("No e-mail subject available.");

		// start a session

		Session session = Session.getDefaultInstance(properties);

		// render message text from template

		VelocityEngine velocity = new VelocityEngine();
		velocity.init();
		VelocityContext context = new VelocityContext();
		context.put("user", this.getUser());
		context.put("newline", "\n");
		StringReader reader = new StringReader(this.getTemplate());
		StringWriter writer = new StringWriter();
		velocity.evaluate(context, writer, this.getClass().getSimpleName(), reader);

		// create message

		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(this.getFrom()));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(this.getTo()));
		message.setSubject(this.getSubject());
		message.setText(writer.toString());

		try {
			message.writeTo(System.out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// send message

		Transport.send(message);
	}

	public User getUser() {

		return this.user;
	}

	public void setUser(User user) {

		this.user = user;
	}

	public String getHost() {

		return this.host;
	}

	public void setHost(String host) {

		this.host = host;
	}

	public String getTo() {

		return this.to;
	}

	public void setTo(String to) {

		this.to = to;
	}

	public String getFrom() {

		return this.from;
	}

	public void setFrom(String from) {

		this.from = from;
	}

	public String getSubject() {

		return this.subject;
	}

	public void setSubject(String subject) {

		this.subject = subject;
	}

	public String getTemplate() {

		return this.template;
	}

	public void setTemplate(String template) {

		this.template = template;
	}
}
