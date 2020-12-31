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

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public final class FindMeetingQuery {
  // Return the possible times at which the meeting be set on the day.
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    List<Event> eventsWithRequiredAttendees = new ArrayList<>();
    eventsWithRequiredAttendees = getEventsForAttendees(events, request.getAttendees(), eventsWithRequiredAttendees);

    List<Event> eventsWithOptionalAttendees = new ArrayList<>();
    eventsWithOptionalAttendees = getEventsForAttendees(events, request.getOptionalAttendees(),
        eventsWithOptionalAttendees);

    // If the request has a meeting's duration longer than a day, there are no possible options.
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    // If there are no events with required or optional attendees, the whole day is available to set up a meeting. 
    if (eventsWithRequiredAttendees.isEmpty() && eventsWithOptionalAttendees.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // If there are no events with required attendees, only consider events with optional attendees. 
    if (eventsWithRequiredAttendees.isEmpty()) {
      Collection<TimeRange> availableTimes = getPossibleTimes(eventsWithOptionalAttendees, request);

      if (availableTimes.isEmpty())
        return Arrays.asList(TimeRange.WHOLE_DAY);

      return availableTimes;
    }

    if (eventsWithOptionalAttendees.isEmpty())
      return getPossibleTimes(eventsWithRequiredAttendees, request);

    return getCommonTimes(eventsWithRequiredAttendees, eventsWithOptionalAttendees, request);
  }

  public List<Event> getEventsForAttendees(Collection<Event> events, Collection<String> attendees,
      List<Event> eventsForAttendees) {

    for (Event event : events) {
      if (!attendees.isEmpty()) {
        for (String attendee : attendees) {
          if (event.getAttendees().contains(attendee) && !eventsForAttendees.contains(event)) {
            eventsForAttendees.add(event);
          }
        }
      }
    }

    return eventsForAttendees;
  }

  public Collection<TimeRange> getPossibleTimes(List<Event> eventsWithAttendees, MeetingRequest request) {

    List<TimeRange> availableTimes = new ArrayList<>();

    for (int counter = 0; counter < eventsWithAttendees.size(); counter++) {
      Event currentEvent = eventsWithAttendees.get(counter);

      // If the array is not empty, then it means there was one event before this one. 
      if (!availableTimes.isEmpty()) {
        Event previousEvent = eventsWithAttendees.get(counter - 1);

        // Remove the last added timerange to consider the next event. 
        availableTimes.remove(availableTimes.size() - 1);

        // If one event fully contains another (nested event), there are 2 possible times: before and after the longest event. 
        if (previousEvent.getWhen().end() > currentEvent.getWhen().start()
            && previousEvent.getWhen().end() > currentEvent.getWhen().end()) {
          addTimerange(availableTimes, TimeRange.fromStartEnd(previousEvent.getWhen().end(), TimeRange.END_OF_DAY, true), request);
          break;
        }

        // If events have no conflict, there are 3 possible times: before the first one, between the 2 events and after the second one.
        long durationBetweenEvents = currentEvent.getWhen().start() - previousEvent.getWhen().end();
        if (request.getDuration() <= durationBetweenEvents)
          availableTimes
              .add(TimeRange.fromStartEnd(previousEvent.getWhen().end(), currentEvent.getWhen().start(), false));

        // If the end of the event is not the end of the day, then the time in between is a possible time. 
        if (currentEvent.getWhen().end() != TimeRange.END_OF_DAY + 1) {
          addTimerange(availableTimes, TimeRange.fromStartEnd(currentEvent.getWhen().end(), TimeRange.END_OF_DAY, true), request);
        }
      }

      // If there were no previous times, then this event is the first one of the day. The possible times are before and after the event.
      else {
        // If the first event of the day starts at the beginning of the day, there are no availabilities before it. 
        if (currentEvent.getWhen().start() != TimeRange.START_OF_DAY)
          addTimerange(availableTimes, TimeRange.fromStartEnd(TimeRange.START_OF_DAY, currentEvent.getWhen().start(), false), request);

        if (currentEvent.getWhen().end() != TimeRange.END_OF_DAY + 1)
          addTimerange(availableTimes, TimeRange.fromStartEnd(currentEvent.getWhen().end(), TimeRange.END_OF_DAY, true), request);  
      }
    }
    return availableTimes;
  }

  //Return the events common to both required and optional attendees
  public Collection<TimeRange> getCommonTimes(List<Event> eventsWithRequiredAttendees,
      List<Event> eventsWithOptionalAttendees, MeetingRequest request) {

    Collection<TimeRange> requiredAttendeesAvailabilities = getPossibleTimes(eventsWithRequiredAttendees, request);
    Collection<TimeRange> optionalAttendeesAvailabilities = getPossibleTimes(eventsWithOptionalAttendees, request);
    Collection<TimeRange> commonAvailabilities = new ArrayList<>();

    for (TimeRange requiredAttendeesTimerange : requiredAttendeesAvailabilities) {
      for (TimeRange optionalAttendeesTimerange : optionalAttendeesAvailabilities) {

        if (optionalAttendeesTimerange.contains(requiredAttendeesTimerange))
          commonAvailabilities.add(requiredAttendeesTimerange);

        else if (requiredAttendeesTimerange.contains(optionalAttendeesTimerange)) {
          commonAvailabilities.add(optionalAttendeesTimerange);
        }

        // Get the intersection between required and optional attendees' availabilities
        else if (optionalAttendeesTimerange.contains(requiredAttendeesTimerange.start())
            && requiredAttendeesTimerange.contains(optionalAttendeesTimerange.end()))
          commonAvailabilities
              .add(TimeRange.fromStartEnd(requiredAttendeesTimerange.start(), optionalAttendeesTimerange.end(), false));

        else if (optionalAttendeesTimerange.contains(requiredAttendeesTimerange.end())
            && requiredAttendeesTimerange.contains(optionalAttendeesTimerange.start())) {
          TimeRange commonTime = TimeRange.fromStartEnd(optionalAttendeesTimerange.start(),
              requiredAttendeesTimerange.end(), false);

          // If the availability after considering optional attendees is smaller than the requested duration, ignore optional attendees. 
          if (commonTime.duration() < request.getDuration())
            commonAvailabilities.add(requiredAttendeesTimerange);
          else
            commonAvailabilities.add(commonTime);
        }
      }
    }

    if (commonAvailabilities.isEmpty())
      return requiredAttendeesAvailabilities;

    return commonAvailabilities;
  }

  // Add the timerange to the available times list only if its duration is equal or greater than the requested meeting duration. 
  public void addTimerange(List<TimeRange> availableTimes, TimeRange timerange, MeetingRequest request) {
    if (request.getDuration() <= timerange.duration()) {
      availableTimes.add(timerange);
    }
  }

}
