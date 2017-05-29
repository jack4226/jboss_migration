package jpa.service.maillist;

import javax.mail.MessagingException;

import jpa.util.EmailSender;

public class TestSubUnsub {

	public static void main(String[] args) {
		TestSubUnsub test = new TestSubUnsub();
		try {
			test.subscribe();
			Thread.sleep(20000);
			test.unsubscribe();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void subscribe() throws MessagingException {
		EmailSender.send("testuser@test.com", "demolist1@localhost", "subscribe", "");
	}

	void unsubscribe() throws MessagingException {
		EmailSender.send("testuser@test.com", "demolist1@localhost", "unsubscribe", "");
	}

}
