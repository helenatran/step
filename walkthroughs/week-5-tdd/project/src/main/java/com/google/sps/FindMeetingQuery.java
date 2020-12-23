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
    if (events.isEmpty() || request.getAttendees().isEmpty()) {
      // If the request has a meeting's duration longer than a day, there are no possible options.
      if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
        return Arrays.asList();
      }

      // If there are no events or no required attendees and the meeting duration is within a day, the whole day is available to set up a meeting. 
      else {
        return Arrays.asList(TimeRange.WHOLE_DAY);
      }
    }
    
    // All the possible times to arrange a meeting on the day.
    List<TimeRange> possibleTimes = new ArrayList<>();

    // All the people who are quired to attend the meeting. 
    Collection<String> requestAttendees = request.getAttendees();

    // Events with required attendees. Use a set to avoid duplicates.
    Collection<Event> requiredEvents = new HashSet<>();

    for (Event event : events) {
      for (String requiredAttendee : requestAttendees) {
        if (event.getAttendees().contains(requiredAttendee)) {
          requiredEvents.add(event);
        }
      }
    }

    if (requiredEvents.size() > 0) {
      // If there is only 1 event, the possible times are before and after the event. 
      if (requiredEvents.size() == 1) {
        Iterator<Event> eventIt = events.iterator();
        Event onlyEvent = eventIt.next();

        possibleTimes.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, onlyEvent.getWhen().start(), false));
        possibleTimes.add(TimeRange.fromStartEnd(onlyEvent.getWhen().end(), TimeRange.END_OF_DAY, true));
        return possibleTimes;
      }

      // for (Event event : requiredEvents) {
      //   if (!possibleTimes.isEmpty()) {

      //   }
      //   else {
      //     possibleTimes.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, event.getWhen().start(), false));
      //     possibleTimes.add(TimeRange.fromStartEnd(event.getWhen().end(), TimeRange.END_OF_DAY, true));
      //   }
       
      // }

      // This assumes there are 2 events with required attendees 
      if (requiredEvents.size() == 2) {
        Iterator<Event> it = events.iterator();
        Event firstEvent = it.next();
        Event secondEvent = it.next();

        if ((secondEvent.getWhen().start() - firstEvent.getWhen().end()) < request.getDuration()) {
          // If one person's event fully contains another's event (nested events), there are 2 possible times: before and after the first event.
          if (firstEvent.getWhen().end() > secondEvent.getWhen().start()
              && firstEvent.getWhen().end() > secondEvent.getWhen().end()) {
            possibleTimes.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, firstEvent.getWhen().start(), false));
            possibleTimes.add(TimeRange.fromStartEnd(firstEvent.getWhen().end(), TimeRange.END_OF_DAY, true));
            return possibleTimes;
          }
          // If the events are overlapping, there are 2 possible times: before the first event and after the second event.
          else if (firstEvent.getWhen().end() > secondEvent.getWhen().start()) {
            possibleTimes.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, firstEvent.getWhen().start(), false));
            possibleTimes.add(TimeRange.fromStartEnd(secondEvent.getWhen().end(), TimeRange.END_OF_DAY, true));
            return possibleTimes;
          } else {
            return Arrays.asList();
          }
        }

        /**
         * If the events are not overlapping but the first meeting starts at the start of the day and
         * the second meeting ends at the end of the day, we have 1 possible time: between these events.
         */
        if (firstEvent.getWhen().start() == TimeRange.START_OF_DAY
            && secondEvent.getWhen().end() == TimeRange.END_OF_DAY + 1) {
          int duration = Math.toIntExact(request.getDuration());
          possibleTimes.add(TimeRange.fromStartDuration(firstEvent.getWhen().end(), duration));
          return possibleTimes;
        }

        // If the events are not overlapping and start/end in the middle of the day, there 3 possible times (in-between the events).
        possibleTimes.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, firstEvent.getWhen().start(), false));
        possibleTimes.add(TimeRange.fromStartEnd(firstEvent.getWhen().end(), secondEvent.getWhen().start(), false));
        possibleTimes.add(TimeRange.fromStartEnd(secondEvent.getWhen().end(), TimeRange.END_OF_DAY, true));
        return possibleTimes;
      }
    }
    
    // If none of the events have required attendees, the whole day is available. 
    else {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    throw new UnsupportedOperationException("There is an error. Ensure your meeting request is correct.");
  }
}
