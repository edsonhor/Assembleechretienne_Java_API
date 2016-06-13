package UnitTest;

import static org.junit.Assert.*;

import javax.mail.MessagingException;

import org.junit.Test;

import HelperClasses.MailService;

public class mailService {

	@Test
	public void testSendEmail() {
		MailService unitUndertest= new MailService();
		try {
			unitUndertest.sendEmail("edson.philippe@ufl.edu", "This is a test", "If you got this mail, then you should be happy");
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
