package com.google.gtc.gaeexample.blog;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class BlogPostController extends HttpServlet {
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    String action = req.getParameter("action");
    String message = req.getParameter("message");
    UserService userService = UserServiceFactory.getUserService();
    User usr = userService.getCurrentUser();
    String currentUserName = "";
    if(usr!=null) {
      currentUserName = usr.getNickname();
    }
    Dao dao = new Dao();
    
    if(action != null && action.equalsIgnoreCase("post")) {
      dao.addBlogEntry(currentUserName, message);
      resp.sendRedirect("/");
    } else if(action != null && action.equalsIgnoreCase("comment")) {
      String postId = req.getParameter("postId");
      dao.addComment(Long.parseLong(postId), currentUserName, message);
      resp.sendRedirect("/?postId=" + postId);
    }
 
    return;
  }
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
 
    Dao dao = new Dao();
    String queryString = ""; //extra parameters to pass on links
    
    String postId = req.getParameter("postId");
    List<Entity> entries;
    if(postId!=null && !postId.equalsIgnoreCase("")) {
      //load only one blog post from datastore
      Long postIdL = Long.parseLong(postId);
      queryString += "&postId=" + postId;
      
      entries = new ArrayList<Entity>();
      entries.add(dao.getBlogEntry(postIdL));
      
      //since we are viewing one entry, get comments
      ArrayList<HashMap<String,Object>> commentList = new ArrayList<HashMap<String,Object>>();
      List<Entity> comments = dao.getComments(postIdL);
      for(Entity comment : comments) {
        HashMap<String,Object> commentObj = new HashMap<String, Object>();
        commentObj.put("name", (String) comment.getProperty("name"));
        commentObj.put("message", (String) comment.getProperty("message"));
        commentList.add(commentObj);
      }
      req.setAttribute("comments", commentList);
      
      
    } else {
      //get all the blog posts from the datastore
      entries = (List<Entity>) dao.getBlogEntries();
    }
    
    //get the list of users this user is following
    List<String> following = dao.getUserFollowing();
    
    ArrayList<HashMap<String,Object>> entities = new ArrayList<HashMap<String,Object>>();
    for(Entity entry : entries) {
      String name = (String) entry.getProperty("name");
      String message = (String) entry.getProperty("message");
      String id = Long.toString(entry.getKey().getId());
      Date postDate = (Date) entry.getProperty("date");
      DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
      DateFormat tf = DateFormat.getTimeInstance(DateFormat.SHORT);
      String dateString = "";
      if(postDate !=null) {
        dateString = df.format(postDate)  + " at " + tf.format(postDate);
      }

      String bFollowing = "false";
      if(following != null && following.contains(name)) {
        bFollowing = "true";
      }
      
      HashMap<String,Object> entity = new HashMap<String, Object>();
      entity.put("name",name);
      entity.put("message",message);
      entity.put("id",id);
      entity.put("postdate", dateString);
      entity.put("following", bFollowing);
      
      entities.add(entity);
    }
    
    
    //The results will be passed back (as an attribute) to the JSP view
    // The attribute will be a name/value pair, the value in this case will be a List object 
    req.setAttribute("entities", entities);
    req.setAttribute("queryString", queryString);
    req.setAttribute("postId", postId);
    RequestDispatcher view = req.getRequestDispatcher("blogs.jsp");
    try {
      view.forward(req, resp);
    } catch (ServletException e) {
      e.printStackTrace();
    }
  }
  
  

}
