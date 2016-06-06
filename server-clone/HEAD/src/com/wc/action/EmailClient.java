package com.wc.action;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

class EmailClient {
	
	private static final String USER_NAME = "youthschat.info@gmail.com";
	private static final String PASSWORD = "Xiaomao8*"; 
	
	static void sendEmail(String toEmail, String code) {
		
 
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
 
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(USER_NAME, PASSWORD);
			}
		  });
 
		try {
 
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("youthschat.info@gmail.com"));
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(toEmail));
			message.setSubject("Verification Code to Reset YouthsChat password");
			message.setText("Hi,"
				+ "\n\nHere is your verification code that you can use to reset your password: "
			    + "\n" + code
			    + "\n\nPlease open your YouthsChat app, press the \"forgot your password\" button to follow the instruction to reset your password."
			    + "\n\n Cheers,");
 
			Transport.send(message);
 
			System.out.println("Done");
 
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
}
