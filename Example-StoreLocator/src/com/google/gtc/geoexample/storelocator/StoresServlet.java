package com.google.gtc.geoexample.storelocator;

import java.io.IOException;
import java.math.BigDecimal;
import javax.servlet.http.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.appengine.api.rdbms.AppEngineDriver;
import com.google.gson.*;


@SuppressWarnings("serial")
public class StoresServlet extends HttpServlet {
  private final String jdbcConnectionString = "jdbc:google:rdbms://geo-gae-example:tutorial/GEOGAETUTORIAL";
  //create table SQL: CREATE TABLE starbucks (name VARCHAR(255), address VARCHAR(255), city VARCHAR(255), state VARCHAR(255), zipcode VARCHAR(15), telephone VARCHAR(25), lat DECIMAL(10,7), lng DECIMAL(10,7), id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY(id));
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    
    String lat = req.getParameter("lat");
    String lng = req.getParameter("lng");
    String radius = req.getParameter("radius");
    
    Gson gson = new Gson();
    List<HashMap<String,String>> markers = new ArrayList<HashMap<String,String>>();
    
    String sql = "";
    if(lat != null && lng != null) {
      Float iLat = Float.parseFloat(lat);
      Float iLng = Float.parseFloat(lng);
      int iRadius = Integer.parseInt(radius);
      sql = "SELECT id, name, address, lat, lng, telephone, ( 3959 * acos( cos( radians(" + iLat + ") ) * cos( radians( lat ) ) * cos( radians( lng ) - radians(" + iLng + ") ) + sin( radians(" + iLat + ") ) * sin( radians( lat ) ) ) ) AS distance FROM starbucks HAVING distance < " + iRadius + " ORDER BY distance LIMIT 0 , 20;";      
    }
    
    Connection c = null;
    try {
      DriverManager.registerDriver(new AppEngineDriver());
      c = DriverManager.getConnection(jdbcConnectionString);
      System.out.println(sql);
      Statement s = c.createStatement();
      ResultSet rs = s.executeQuery(sql);

      while(rs.next()) {
        int iStore = rs.getInt(1);
        String name = rs.getString(2);
        name = name.replaceAll("&", "&amp;");
        String address = rs.getString(3);
        BigDecimal iLat = rs.getBigDecimal(4);
        BigDecimal iLng = rs.getBigDecimal(5);
        String telephone = rs.getString(6);
        BigDecimal iDist = rs.getBigDecimal(7);
        
        HashMap<String,String> output = new HashMap<String, String>();
        output.put("id", Integer.toString(iStore));
        output.put("name", name);
        output.put("address", address);
        output.put("lat", iLat.toString());
        output.put("lng", iLng.toString());
        output.put("distance", iDist.toString());
        output.put("telephone", telephone);
        markers.add(output);
      
         
        
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (c != null)
        try {
          c.close();
        } catch (SQLException ignore) {
        }
    }
    
   
    resp.getWriter().println(gson.toJson(markers));
   
    return;
  }
  
  
}
