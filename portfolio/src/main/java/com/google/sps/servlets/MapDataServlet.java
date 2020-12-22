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

import com.google.sps.data.MapMarker;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible to display the map markers data. */
@WebServlet("/map-data")
public class MapDataServlet extends HttpServlet {

  private Collection<MapMarker> mapMarkers;

  @Override
  public void init() {
    mapMarkers = new ArrayList<>();

    Scanner scanner = new Scanner(getServletContext().getResourceAsStream("/WEB-INF/map-dataset.csv"));
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] cells = line.split(",");

      if (cells.length == 4) {
        double lat = Double.parseDouble(cells[0]);
        double lng = Double.parseDouble(cells[1]);
        String title = cells[2];
        String description = cells[3];
        mapMarkers.add(new MapMarker(lat, lng, title, description));
      }
      else {
        System.out.println("There are missing data. Make sure you have provided the lat, lng, title, and description of the marker.");
      }      
    }
    scanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(mapMarkers);
    response.getWriter().println(json);
  }
}
