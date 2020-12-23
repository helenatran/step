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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.FunctionsLibrary;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for authentication. */
@WebServlet("/login")
public class AuthenticationServlet extends HttpServlet {
  Gson gson = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    UserService userService = UserServiceFactory.getUserService();

    // If user is not logged in, show a login form.
    if (!userService.isUserLoggedIn()) {
      String loginUrl = userService.createLoginURL("/login");
      out.println("<p>Hello stranger.</p>");
      out.println("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
      return;
    }

    // If user has not set a nickname, redirect to nickname page.
    String nickname = FunctionsLibrary.getUserNickname(userService.getCurrentUser().getUserId());
    if (nickname == "") {
      response.sendRedirect("/nickname");
      return;
    }

    // User is logged in and has a nickname, so the request can proceed.
    String logoutUrl = userService.createLogoutURL("/login");
    String userEmail = userService.getCurrentUser().getEmail();
    out.println("<h1>Home</h1>");
    out.println("<p>Hello " + nickname + "!</p>");
    out.println("<p>Email: " + userEmail + "</p>");
    out.println("<p>Logout <a href=\"" + logoutUrl + "\">here</a>.</p>");
    out.println("<p>Change your nickname <a href=\"/nickname\">here</a>.</p>");
  }

  @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      response.setContentType("text/html");

      UserService userService = UserServiceFactory.getUserService();
      Boolean isUserLoggedIn;
      if (userService.isUserLoggedIn()) {
        isUserLoggedIn = true;
      } else {
        isUserLoggedIn = false;
      }
      // Send the JSON as response.
      response.setContentType("application/json;");
      response.getWriter().println(gson.toJson(isUserLoggedIn));
    }
}
