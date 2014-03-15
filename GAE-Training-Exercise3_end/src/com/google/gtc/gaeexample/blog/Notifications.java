package com.google.gtc.gaeexample.blog;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.apphosting.api.ApiProxy;

public class Notifications {
  private static final Logger log = Logger.getLogger(Notifications.class.getName());

  public void sendFollowerNotification(String posterEmail, String recipientEmail, String postId) {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    String appId = ApiProxy.getCurrentEnvironment().getAppId(); 
    // may be in the form of s~appid or eu~appid (in EU cluster), etc.
    appId = appId.replaceAll("[^~]*~", ""); // cleanup app id details
    
    String htmlBody = "A person you are following, " + posterEmail + ", has posted a new entry.<br/>\n";
    htmlBody += "<a href='http://" + appId + ".appspot.com/?postId=" + postId + "'>View online</a><br/>\n";
    

    try {
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("jhartung@gmail.com", "gae-java-basics-exercise Admin"));
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        msg.setSubject("New blog post by " + posterEmail);
        msg.setContent(htmlBody, "text/html");
        Transport.send(msg);
        System.out.println("message sent to ["+ recipientEmail + "]:" + htmlBody);
    } catch (AddressException e) {
      log.warning(e.getMessage());
      e.printStackTrace();
    } catch (MessagingException e) {
      log.warning(e.getMessage());
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      log.warning(e.getMessage());
      e.printStackTrace();
    }
    
    
  }
  
  
}
