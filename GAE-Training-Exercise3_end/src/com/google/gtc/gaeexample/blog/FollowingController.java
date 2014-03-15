package com.google.gtc.gaeexample.blog;

import java.io.IOException;
import javax.servlet.http.*;


import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class FollowingController extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    
    String action = req.getParameter("action");
    String targetUserName = req.getParameter("name");
    String postId = req.getParameter("postId");
    
    
    UserService userService = UserServiceFactory.getUserService();
    User usr = userService.getCurrentUser();
    String currentUserName = "";
    if(usr!=null) {
      currentUserName = usr.getNickname();
    }
    
    Dao dao = new Dao();
    
    if(action != null && action.equalsIgnoreCase("follow") && targetUserName != null && !targetUserName.equalsIgnoreCase("")) {
      dao.addFollower(targetUserName, currentUserName);
    } else if(action != null && action.equalsIgnoreCase("unfollow") && targetUserName != null && !targetUserName.equalsIgnoreCase("")) {
       dao.unFollow(targetUserName, currentUserName);
    }
    
    //redirect the user to the root
    if(postId!= null && !postId.equalsIgnoreCase("")) {
      resp.sendRedirect("/?postId=" + postId);
    } else {
      resp.sendRedirect("/");
    }
    return;
  }
}
