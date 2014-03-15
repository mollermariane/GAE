<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>Google Maps AJAX + Cloud SQL Example</title>
    
    <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyBC6UuMg5HzWFdliYVZNbQd-ebKLdXOxrU&amp;sensor=true&libraries=visualization"></script>
    <script type="text/javascript" src="/jquery-1.7.1.min.js" charset="utf-8"></script>
    <script type="text/javascript">
    //<![CDATA[
    var map;
    var geocoder;
    var myLat = 38.892781;
    var myLng = -97.802436;
    var markersArray = new Array(); //hold all the geocoding result data for temporary display
    var infowindow = new Object();
    var hashPosts = new Object();
    
    function initialize() {
      geocoder = new google.maps.Geocoder();
      var myLatlng = new google.maps.LatLng(myLat, myLng);
      var myOptions = {
        center : new google.maps.LatLng(myLat, myLng),
        zoom : 13,
        mapTypeId : google.maps.MapTypeId.ROADMAP
      };
      map = new google.maps.Map(document.getElementById("map"), myOptions);
      
      //Check for geolocation support, center on current location if yes
      if (navigator.geolocation) {
        //Use method getCurrentPosition to get coordinates
        navigator.geolocation.getCurrentPosition(function (position) {
          //Access them accordingly
          myLat = position.coords.latitude;
          myLng = position.coords.longitude;
          map.setCenter(new google.maps.LatLng(myLat, myLng));
          searchLocationsNear(myLat,myLng);
          
        });
      }
    }
    
   function searchLocations() {
     var address = document.getElementById('addressInput').value;
     
     geocoder.geocode({
       address : address
     }, function(geoResults, GeocoderStatus) {
       //this is the callback, which is called along with the geocoder results
       if (GeocoderStatus == google.maps.GeocoderStatus.OK) {
         if(geoResults.length > 0) 
           //only use first result
           var myLatlngResult = geoResults[0].geometry.location;
           map.setCenter(myLatlngResult);
           searchLocationsNearCt(myLatlngResult);
           
       } else {
         alert("Geocode was not successful for the following reason: " + status);
       }
     });
   }

   function searchLocationsNearCt(center) {
     searchLocationsNear(center.lat(), center.lng());
   }
   function searchLocationsNear(lat,lng) {
     var radius = document.getElementById('radiusSelect').value;
     var searchUrl = '/stores?lat=' + lat + '&lng=' + lng + '&radius=' + radius + "&type=json";
     //clear out the sidebar
     $("#sidebar").html("");
     
     //perform the ajax call and load the results on the map
     $.ajax({
       type: 'GET',
       url: searchUrl,
       data: '',
       dataType: 'json',
       async: true,
       success: function(msg) {
         if(msg != null) {
           //loop through msg data, json object
           for(var ix in msg) {
            //get results and display on map:
            var marker = new google.maps.Marker({
              position: new google.maps.LatLng(msg[ix].lat, msg[ix].lng), 
              map: map,
              title:msg.name
            });
           
            //store the marker in a hash for later triggers
            hashPosts[msg[ix].id] = marker;          
            var div = createSidebarEntry(marker, msg[ix].id, msg[ix].name, msg[ix].address, msg[ix].lat, msg[ix].lng, msg[ix].distance);
            markersArray.push(marker);
          }
         }
       },
       complete: function(jqXHR, textStatus) {}
       });
     
   }

    
    function createSidebarEntry(marker, id, name, address, lat, lng, distance) {
      var div = document.createElement('div');
      div.id = "store_" + id;
      var html = '<b>' +  name + '</b> (' + distance.substring(0,distance.indexOf(".") + 2) + ' mi.)<br/>' + address;
      div.innerHTML = html;
      div.style.cursor = 'pointer';
      div.style.marginBottom = '5px';
      
      infowindow[id] = new google.maps.InfoWindow({
        content: '<b>' +  name + '</b><br/> (' + distance.substring(0,distance.indexOf(".") + 2) + ' mi.)<br/>' + address
      });

      google.maps.event.addListener(hashPosts[id], 'click', function() {
        infowindow[id].open(map,hashPosts[id]);
      });
      
      //append the HTML to the sidebar
      $("#sidebar").append(div);
      //bind click on sidebar item, show infowindow on map
      $("#store_" + id).bind('click',function() {
        infowindow[id].open(map,hashPosts[id]);
      });
      
    }
    
    
    //]]>
  </script>
  </head>

  <body onload="initialize()">
    Address: <input type="text" id="addressInput"/>
    Radius: <select id="radiusSelect">
      <option value="25" selected>25</option>
      <option value="100">100</option>
      <option value="200">200</option>
    </select>
    <input type="button" onclick="searchLocations()" value="Search Locations"/>
    <br/>    
    <br/>
<div style="width:800px; font-family:Arial, sans-serif; font-size:11px; border:1px solid black">
  <table> 
    <tbody> 
      <tr id="cm_mapTR">
        <td width="200" valign="top"> <div id="sidebar" style="overflow: auto; height: 400px; font-size: 11px; color: #000"></div>
        </td>
        <td> <div id="map" style="overflow: hidden; width:600px; height:400px"></div> </td>
      </tr> 
    </tbody>
  </table>
</div>    
<br/>
  </body>
</html>