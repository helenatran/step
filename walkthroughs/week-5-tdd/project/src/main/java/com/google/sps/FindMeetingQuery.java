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

    // All the possible times to arrange a meeting on the day.
    List<TimeRange> possibleTimes = new ArrayList<>();

    // All the people who are required to attend the meeting. 
    Collection<String> requestAttendees = request.getAttendees();

    // All the people whose attendance is optional. 
    Collection<String> optionalAttendees = request.getOptionalAttendees();

    // Events with required attendees. We assume events are always added in a chronological order. 
    List<Event> requiredEvents = new ArrayList<>();

    // Events with optional attendees. We assume events are always added in a chronological order. 
    List<Event> optionalEvents = new ArrayList<>();

    if (events.isEmpty() || (requestAttendees.isEmpty() && optionalAttendees.isEmpty())) {
      // If the request has a meeting's duration longer than a day, there are no possible options.
      if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
        return Arrays.asList();
      }

      // If there are no events or no required attendees and the meeting duration is within a day, the whole day is available to set up a meeting. 
      else {
        return Arrays.asList(TimeRange.WHOLE_DAY);
      }
    }

    for (Event event : events) {
      for (String requiredAttendee : requestAttendees) {
        if (event.getAttendees().contains(requiredAttendee)) {
          if (!requiredEvents.contains(event)) {
            requiredEvents.add(event);
          }
        }
      }

      if (!optionalAttendees.isEmpty()) {
        for (String optionalAttendee : optionalAttendees) {
          if (event.getAttendees().contains(optionalAttendee)) {
            if (!optionalEvents.contains(event)) {
              optionalEvents.add(event);
            }
          }
        }
      }
    }

    if (requiredEvents.size() > 0 || (requiredEvents.isEmpty() && !optionalEvents.isEmpty())) {
      // If there is only 1 event, the possible times are before and after the event. 
      if (requiredEvents.size() == 1 || (requiredEvents.isEmpty() && optionalEvents.size() == 1)) {
        Iterator<Event> eventIt = events.iterator();
        Event onlyEvent = eventIt.next();

        possibleTimes.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, onlyEvent.getWhen().start(), false));
        possibleTimes.add(TimeRange.fromStartEnd(onlyEvent.getWhen().end(), TimeRange.END_OF_DAY, true));
        return possibleTimes;
      }

      // Events List chosen to check availabilities. Depends on whether there are mandatory and/or optional attendees.
      List<Event> chosenEventsList = new ArrayList<>();
      
      // If there are no events with mandatory attendees, but only optional attendees, then we should treat the optional events as required events. 
      if (requiredEvents.isEmpty() && !optionalEvents.isEmpty()) {
        chosenEventsList = optionalEvents;
      }
      else {
        chosenEventsList = requiredEvents;
      }

      for (int counter = 0; counter < chosenEventsList.size(); counter++) {
        Event currentEvent = chosenEventsList.get(counter);

        // If the array is not empty, then it means there was one event before this one. 
        if (!possibleTimes.isEmpty()) {
          Event previousEvent = chosenEventsList.get(counter - 1);

          // Remove the last added timerange to consider the unavailability due to the current event. 
          possibleTimes.remove(possibleTimes.size() - 1);

          // If one event fully contains another (nested event), there are 2 possible times: before and after the longest event. 
          if (previousEvent.getWhen().end() > currentEvent.getWhen().start()
              && previousEvent.getWhen().end() > currentEvent.getWhen().end()) {
            possibleTimes.add(TimeRange.fromStartEnd(previousEvent.getWhen().end(), TimeRange.END_OF_DAY, true));
            break;
          }

          // If events have no conflict, there are 3 possible times: before the first one, between the 2 events and after the second one.
          if (previousEvent.getWhen().end() < currentEvent.getWhen().start()) {
            // Duration available in between events. 
            long durationBetweenEvents = currentEvent.getWhen().start() - previousEvent.getWhen().end();

            // Check if the requested duration matches with the time available in between events. 
            if (request.getDuration() <= durationBetweenEvents) {
              possibleTimes
                  .add(TimeRange.fromStartEnd(previousEvent.getWhen().end(), currentEvent.getWhen().start(), false));
            }
            else if (chosenEventsList == optionalEvents) {
              possibleTimes.add(TimeRange.WHOLE_DAY);
            }
          }

          // If the end of the event is not the end of the day, then the time in between is a possible time. 
          if (currentEvent.getWhen().end() != TimeRange.END_OF_DAY + 1) {
            possibleTimes.add(TimeRange.fromStartEnd(currentEvent.getWhen().end(), TimeRange.END_OF_DAY, true));
          }

        }

        // If there were no previous times, then this event is the first one of the day. The possible times are before and after the event.
        else {
          // If the first event of the day starts at the beginning of the day, there are no availabilities before it. 
          if (currentEvent.getWhen().start() != TimeRange.START_OF_DAY) {
            possibleTimes.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, currentEvent.getWhen().start(), false));
          }
          possibleTimes.add(TimeRange.fromStartEnd(currentEvent.getWhen().end(), TimeRange.END_OF_DAY, true));
        }
      }
      
      // Check with optional attendees before returning
      // 1. Is there optional attendees?
      // 2. Are their availabilities matching with the possible times? 
      if (!requiredEvents.isEmpty() && !optionalEvents.isEmpty()) {
        for (Event optionalEvent : optionalEvents) {
          if (possibleTimes.contains(optionalEvent.getWhen())) {
            possibleTimes.remove(optionalEvent.getWhen());
          }
        }
      }
      return possibleTimes;
    }
    
    // If none of the events have required attendees, the whole day is available. 
    else {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
  }
}
