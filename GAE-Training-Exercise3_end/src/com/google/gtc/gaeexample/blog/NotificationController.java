package com.google.gtc.gaeexample.blog;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class NotificationController extends HttpServlet {
  private static final Logger log = Logger.getLogger(NotificationController.class.getName());
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    log.info("New call to the /notification handler");
    
    String recipientEmail = req.getParameter("recipientEmail");
    String posterEmail = req.getParameter("posterEmail");
    String postId = req.getParameter("postId");
    String action = req.getParameter("action");
    
    Notifications notif = new Notifications();
    
    if(action != null && action.equalsIgnoreCase("sendFollowerNotification")) {
      notif.sendFollowerNotification(posterEmail, recipientEmail, postId);
    }
    
    return;
  }
}
