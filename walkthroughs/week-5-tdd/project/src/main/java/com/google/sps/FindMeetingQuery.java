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

public final class FindMeetingQuery {
  // Return the times when the meeting could happen that day.
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (events.isEmpty()) {
      // If the request has the meeting's duration longer than a day, there are no possible options.
      if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
        return Arrays.asList();
      }

      // If there are no events and the duration given is not longer than a day, the whole day is available to set up a meeting. 
      else {
        return Arrays.asList(TimeRange.WHOLE_DAY);
      }
    }

    /**
     * If there is an event during the day, there are 
     * ToDo(Helena): consider when there are more than 1 event
     */
    if (events.size() == 1) {
      Collection<TimeRange> possibleTimes = new ArrayList<>();
      Iterator<Event> eventIt = events.iterator();
      Event onlyEvent = eventIt.next();
      Iterator<String> eventAttendeeIt = onlyEvent.getAttendees().iterator();
      String eventAttendee = eventAttendeeIt.next();

      // If the event is linked to a non-required attendee, the whole day should be available.
      Iterator<String> requestAttendeeIt = request.getAttendees().iterator();
      String requiredAttendee = requestAttendeeIt.next();

      if (eventAttendee != requiredAttendee) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
      }

      // If the event is linked to a required attendee, there are 2 possible times: before and after the event.
      else {
        possibleTimes.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, onlyEvent.getWhen().start(), false));
        possibleTimes.add(TimeRange.fromStartEnd(onlyEvent.getWhen().end(), TimeRange.END_OF_DAY, true));
        return possibleTimes;
      }
    }

    /**
     * If there are 2 events linked to required attendees, there are multiple scenarios possible:
     * ToDo(Helena):
     *  - Consider when there are more than 2 attendees.
     *  - Consider when they have more than 1 event per attendee.
     * We assume that events are always added chronologically (based on their start time).
     */
    if (events.size() == 2) {
      Collection<TimeRange> possibleTimes = new ArrayList<>();
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
        }  
        else {
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

    throw new UnsupportedOperationException("TODO: Implement this method.");
  }
}
