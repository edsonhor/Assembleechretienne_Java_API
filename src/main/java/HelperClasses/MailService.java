package HelperClasses;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

public class MailService {
	
	
	public MailService(){
		
	}
	
	public static boolean sendEmail(String to, String subject, String body) throws AddressException{
		
		 final String username = "info@assembleechretienne.com";
		 final String password = "";
		 
		 Properties prop=new Properties();
         prop.put("mail.smtp.auth", "true");
         prop.put("mail.smtp.host", "assembleechretienne.com");
         prop.put("mail.smtp.port", "587");
         prop.put("mail.smtp.starttls.enable", "true");
         prop.put("mail.smtp.localhost", "assembleechretienne.com");

       Session session = Session.getDefaultInstance(prop,
       new javax.mail.Authenticator() {
         protected PasswordAuthentication getPasswordAuthentication() {
             return new PasswordAuthentication(username, password);
       }
     });
         session.setDebug(true);
                  
         try {
        	 
             
             String textBody = body;
             MimeMessage message= new MimeMessage(session);
                              
         message.setFrom(new InternetAddress("info@assembleechretienne.com"));
             message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to));
    message.setSubject(subject);

        message.setText(body);
                    message.setContent(textBody, "text/html;");
        Transport.send(message);

    } catch (MessagingException e) {
        e.printStackTrace();
        return false;
    } 
         /*catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
         return true;
}

         
      
	}
	
