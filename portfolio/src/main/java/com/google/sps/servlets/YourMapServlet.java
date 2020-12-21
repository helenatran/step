// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.MapMarker;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handles fetching and saving markers data. */
@WebServlet("/your-map-data")
public class YourMapServlet extends HttpServlet {

  Gson gson = new Gson();

  /** Responds with a JSON array containing marker data. */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Marker");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<MapMarker> markers = new ArrayList<>();

    for (Entity entity : results.asIterable()) {
      double lat = (double) entity.getProperty("lat");
      double lng = (double) entity.getProperty("lng");
      String title = (String) entity.getProperty("title");
      String description = (String) entity.getProperty("description");

      MapMarker marker = new MapMarker(lat, lng, title, description);
      markers.add(marker);
    }

    // Send the JSON as response.
    response.setContentType("application/json");
    String json = gson.toJson(markers);
    response.getWriter().println(json);
  }

  /** Accepts a POST request containing a new marker. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    // Only logged-in users can add a marker
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/login");
      return;
    }

    // Get inputs from the form.
    double lat = Double.parseDouble(request.getParameter("lat"));
    double lng = Double.parseDouble(request.getParameter("lng"));
    String title = getUserNickname(userService.getCurrentUser().getUserId());
    String description = request.getParameter("description");

    MapMarker marker = new MapMarker(lat, lng, title, description);

    Entity markerEntity = new Entity("Marker");
    markerEntity.setProperty("lat", lat);
    markerEntity.setProperty("lng", lng);
    markerEntity.setProperty("title", title);
    markerEntity.setProperty("description", description);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(markerEntity);

    response.sendRedirect("/myMap.html");
  }

  /** Returns the nickname of the user with id, or null if the user has not set a nickname. */
  private String getUserNickname(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return null;
    }
    String nickname = (String) entity.getProperty("nickname");
    return nickname;
  }
}
