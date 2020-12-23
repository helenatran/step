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

package com.google.sps.data;

/** A marker on my map. */
public class MapMarker {
  private double lat;
  private double lng;
  private String title;
  private String description;

    /** Create a new map marker. 
   *  @params lat           the latitude of the location indicated by the marker. 
   *  @params lng           the longitude of the location indicated by the marker. 
   *  @params title         the title of the location indicated by the marker. 
   *  @params description   the description of the location indicated by the marker. 
  */
  public MapMarker(double lat, double lng, String title, String description) {
    this.lat = lat;
    this.lng = lng;
    this.title = title;
    this.description = description;
  }
}
