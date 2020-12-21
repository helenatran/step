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

public final class FindMeetingQuery {
  // Return the times when the meeting could happen that day.
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // If the request has no compulsory attendees and there are no events on the day, meeting can be done at any time.
    if (request.getAttendees().isEmpty() && events.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // If the request has the meeting's duration longer than a day, there are no possible options.
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    /**
     * If there is an event during the day, the possible times are before and after the event.
     * ToDo(Helena): consider when there are more than 1 event
     */
    if (events.size() == 1) {
      Collection<TimeRange> possibleTimes = new ArrayList<>();
      for (Event event : events) {
        possibleTimes.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, event.getWhen().start(), false));
        possibleTimes.add(TimeRange.fromStartEnd(event.getWhen().end(), TimeRange.END_OF_DAY, true));
      }
      return possibleTimes;
    }

    /**
     * If there are 2 attendees, the possible times are the ones in-between their events.
     * ToDo(Helena):
     *  - First, implement the function as stated above, then: 
     *  - Consider when there are more than 2 attendees.
     *  - Consider when they have more than 1 event per attendee.
     */
    throw new UnsupportedOperationException("TODO: Implement this method.");
  }
}
