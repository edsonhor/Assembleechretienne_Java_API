package UnitTest;

import javax.mail.MessagingException;

import org.junit.Test;

import HelperClasses.MailService;

public class mailService {

	@Test
	public void testSendEmail() {
		try {
			MailService.sendEmail("edson.philippe@ufl.edu", "This is a test", "If you got this mail, then you should be happy");
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
