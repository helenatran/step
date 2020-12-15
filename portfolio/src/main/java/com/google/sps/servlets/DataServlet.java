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
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Servlet responsible for creating and displaying comments.
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  Gson gson = new Gson();

  // Get the comments.
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String username = (String) entity.getProperty("username");
      String commentText = (String) entity.getProperty("commentText");
      long timestamp = (long) entity.getProperty("timestamp");

      Comment comment = new Comment(id, username, commentText, timestamp);
      comments.add(comment);
    }

    // Send the JSON as response.
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  // Create new comment.
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get inputs from the form.
    String username = request.getParameter("username");
    String commentText = request.getParameter("commentText");
    long timestamp = System.currentTimeMillis();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("username", username);
    commentEntity.setProperty("commentText", commentText);
    commentEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.sendRedirect("/index.html");
  }
}
