package com.google.gtc.gaeexample.blog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;


public class Dao {
  private static final Logger log = Logger.getLogger(Dao.class.getName());
  
  public Entity addComment(Long postId, String currentUserName, String message) {
    //**Exercise**: store "Comments"
    Key blogKey = KeyFactory.createKey("Entradas", postId);
    //create new comment entity with the given BlogEntry as it's ancestor
    Entity comment = new Entity("Comentario", blogKey);
    comment.setProperty("name", currentUserName);
    comment.setProperty("message", message);
    comment.setProperty("date", new Date());
    
    DatastoreService repositorio = DatastoreServiceFactory.getDatastoreService();
    repositorio.put(comment);
    
    return comment;
  }
  
  public List<Entity> getComments(Long postId) {
    //**Exercise**: store "Comments"
    DatastoreService repositorio = DatastoreServiceFactory.getDatastoreService();
    Key idEntrada = KeyFactory.createKey("Entradas", postId);
    Query q = new Query("Comentario");
    q.setAncestor(idEntrada);
    //q.addSort("date");
    FetchOptions limite = FetchOptions.Builder.withLimit(500);
    List<Entity> listaComentarios = (List<Entity>) repositorio.prepare(q).asList(limite);

    return listaComentarios;
  }
  
  
  /**
   * Record that the logged in user is following the target user
   * @param targetUserName
   * @param currentUserName
   */
  public void addFollower(String targetUserName, String currentUserName) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    {
      //1. get followingEntity for the current user
      Key followingKey = KeyFactory.createKey("Following", currentUserName);
      Entity followingEntity;
      try {
        followingEntity = datastore.get(followingKey);
      } catch(EntityNotFoundException ex) {
        //not found, so create new!
        followingEntity = new Entity(followingKey);
        followingEntity.setProperty("email", currentUserName);
      }
      
      ArrayList<String> following;
      if(!followingEntity.hasProperty("following")) {
        following = new ArrayList<String>();
      } else {
        following = (ArrayList<String>)followingEntity.getProperty("following");
      }
      
      if(following == null) {
        following = new ArrayList<String>();
      }
      
      //add new entry to following for this person
      if(!following.contains(targetUserName)) {
        following.add(targetUserName);
      }
      followingEntity.setUnindexedProperty("following", following);
      datastore.put(followingEntity);
      log.info(currentUserName + " is following:" + following);
      
    }
    
    {
      //2. now get followingEntity for the target user
      Key targetUserKey = KeyFactory.createKey("Following", targetUserName);
      Entity targetUserEntity;
      try {
        targetUserEntity = datastore.get(targetUserKey);
      } catch(EntityNotFoundException ex) {
        //not found, so create new!
        targetUserEntity = new Entity(targetUserKey);
        targetUserEntity.setProperty("email", targetUserName);
      }
      
      ArrayList<String> followers;
      if(!targetUserEntity.hasProperty("followers")) {
        followers = new ArrayList<String>();
      } else {
        followers = (ArrayList<String>)targetUserEntity.getProperty("followers");
      }
      
      if(followers == null) {
        followers = new ArrayList<String>();
      }
      
      //add new entry to following for this person
      if(!followers.contains(currentUserName)) {
        followers.add(currentUserName);
      }
      targetUserEntity.setUnindexedProperty("followers", followers);
      log.info("followers of " + targetUserName + ":" + followers);
      datastore.put(targetUserEntity);
    }
    log.info("Following data persisted");
  }
  
  /**
   * Record that the logged in user is no longer following the target user
   * @param targetUserName
   * @param currentUserName
   */
  public void unFollow(String targetUserName, String currentUserName) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    {
      //1. get followingEntity for the current user
      Key followingKey = KeyFactory.createKey("Following", currentUserName);
      Entity followingEntity;
      try {
        followingEntity = datastore.get(followingKey);
      } catch(EntityNotFoundException ex) {
        //not found, so create new!
        followingEntity = new Entity(followingKey);
        followingEntity.setProperty("email", currentUserName);
      }
      
      ArrayList<String> following;
      if(!followingEntity.hasProperty("following")) {
        following = new ArrayList<String>();
      } else {
        following = (ArrayList<String>)followingEntity.getProperty("following");
      }
      
      if(following == null) {
        following = new ArrayList<String>();
      }
      
      //remove following entry for the logged in user
      if(following.contains(targetUserName)) {
        following.remove(targetUserName);
      }
      followingEntity.setUnindexedProperty("following", following);
      datastore.put(followingEntity);
      log.info(currentUserName + " is following:" + following);
    }
    
    {
      //2. now get followingEntity for the target user
      Key targetUserKey = KeyFactory.createKey("Following", targetUserName);
      Entity targetUserEntity;
      try {
        targetUserEntity = datastore.get(targetUserKey);
      } catch(EntityNotFoundException ex) {
        //not found, so create new!
        targetUserEntity = new Entity(targetUserKey);
        targetUserEntity.setProperty("email", targetUserName);
      }
      
      ArrayList<String> followers;
      if(!targetUserEntity.hasProperty("followers")) {
        followers = new ArrayList<String>();
      } else {
        followers = (ArrayList<String>)targetUserEntity.getProperty("followers");
      }
      if(followers == null) {
        followers = new ArrayList<String>();
      }
      //remove followers entry from target user
      if(followers.contains(currentUserName)) {
        followers.remove(currentUserName);
      }
      targetUserEntity.setUnindexedProperty("followers", followers);
      log.info("followers of " + targetUserName + ":" + followers);
      datastore.put(targetUserEntity);
    }
    log.info("Following data persisted");
  }
  
  
  /**
   * Get the entity for this user. This includes the people they are following as well as the people following this user.
   * @param email
   * @return
   */
  public List<String> getFollowers(String email) {
    if(email.equalsIgnoreCase("")) {
      return new ArrayList<String>();
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key userKey = KeyFactory.createKey("Following", email);
    Entity userEntity;
    List<String> followers = null;;
    try {
      userEntity = datastore.get(userKey);
      followers = (List<String>)userEntity.getProperty("followers");
      
    } catch(EntityNotFoundException ex) {
    }
    
    if(followers == null) {
      followers = new ArrayList<String>();
    }
    return followers;
  }
  
  
  /** get the list of users the logged in user is following
   * 
   * @return
   */
  public List<String> getUserFollowing() {
    UserService userService = UserServiceFactory.getUserService();
    User usr = userService.getCurrentUser();
    if(usr!=null) {
      String currentUserName = usr.getNickname();
      return getUserFollowing(currentUserName);
    } else {
      return new ArrayList<String>();
    }
  }
  /**
   * Get the list of users this person is following
   * @param email
   * @return Set of email address this user is following
   */
  public List<String> getUserFollowing(String email) {
    ArrayList<String> following = new ArrayList<String>();
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key userKey = KeyFactory.createKey("Following", email);
    Entity userEntity;
    try {
      userEntity = datastore.get(userKey);
      following = (ArrayList<String>)userEntity.getProperty("following");
    } catch(EntityNotFoundException ex) {
      //not found, so return empty set
    }
    return following;
  }
  
  public Entity addBlogEntry(String currentUserName, String message) {
    //create a new, empty Entity of type "BlogEntry"
    Entity blogEntry = new Entity("BlogEntry");
    
    blogEntry.setProperty("name", currentUserName);
    
    if(message != null && !message.equalsIgnoreCase("")) {
      //it would be a good idea to cleanse this input, i.e. remove <script> 
      //or even html encode all the special characters to prevent cross site 
      //scripting attacks
      blogEntry.setProperty("message", message);
      
      //store the current date/time
      Date postDate = new Date();
      blogEntry.setProperty("date", postDate);
      
      //persist the entity in the datastore
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(blogEntry);
      log.info("Blog entry persisted");
      
      //Notify followers
      notifyFollowers(blogEntry);
      
    }
    return blogEntry;
  }
  
  public List<Entity> getBlogEntries() {
    //get all the blog posts from the datastore 
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query q = new Query("BlogEntry");
    FetchOptions limit = FetchOptions.Builder.withLimit(500);
    q.addSort("date", SortDirection.DESCENDING);
    
    List<Entity> entries = (List<Entity>) ds.prepare(q).asList(limit);
    return entries;
  }
  
  public Entity getBlogEntry(Long postId) {
    //get one blog post from the datastore 
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Key blogKey = KeyFactory.createKey("BlogEntry", postId);
    Entity blogEntry;
    try {
      blogEntry = ds.get(blogKey);
    } catch (EntityNotFoundException e) {
      e.printStackTrace();
      blogEntry = new Entity("BlogEntry");
    }
    return blogEntry;
  }
  
  public void notifyFollowers(Entity blogEntry) {
    //Send e-mail notification to all users that are following the author of this post
    String posterEmail = (String)blogEntry.getProperty("name");
    String postId = Long.toString(blogEntry.getKey().getId());

    //**Exercise**: Switch sending e-mail to the background
    Notifications note = new Notifications();
    //look up followers
    List<String> followers = getFollowers(posterEmail);
    for(String recipientEmail : followers){
      //send notification:
      //note.sendFollowerNotification(posterEmail, recipientEmail, postId);
    	Queue fila = QueueFactory.getDefaultQueue();
        fila.add(TaskOptions.Builder.withUrl("/notification")
        		.param("posterEmail", posterEmail)
        		.param("recipientEmail", recipientEmail)
        		.param("postId", postId)
        		.param("action", "sendFollowerNotification"));
    }
  }

  
  
  
}
