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
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class FindMeetingQueryTest {
  private static final Collection<Event> NO_EVENTS = Collections.emptySet();
  private static final Collection<String> NO_ATTENDEES = Collections.emptySet();

  // Some people that we can use in our tests.
  private static final String PERSON_A = "Person A";
  private static final String PERSON_B = "Person B";
  private static final String PERSON_C = "Person C";

  // All dates are the first day of the year 2020.
  private static final int TIME_0800AM = TimeRange.getTimeInMinutes(8, 0);
  private static final int TIME_0830AM = TimeRange.getTimeInMinutes(8, 30);
  private static final int TIME_0900AM = TimeRange.getTimeInMinutes(9, 0);
  private static final int TIME_0930AM = TimeRange.getTimeInMinutes(9, 30);
  private static final int TIME_1000AM = TimeRange.getTimeInMinutes(10, 0);
  private static final int TIME_1100AM = TimeRange.getTimeInMinutes(11, 00);

  private static final int DURATION_15_MINUTES = 15;
  private static final int DURATION_30_MINUTES = 30;
  private static final int DURATION_60_MINUTES = 60;
  private static final int DURATION_90_MINUTES = 90;
  private static final int DURATION_1_HOUR = 60;
  private static final int DURATION_2_HOUR = 120;

  private FindMeetingQuery query;

  @Before
  public void setUp() {
    query = new FindMeetingQuery();
  }

  @Test
  public void optionsForNoAttendees() {
    MeetingRequest request = new MeetingRequest(NO_ATTENDEES, DURATION_1_HOUR);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noOptionsForTooLongOfARequest() {
    // The duration should be longer than a day. This means there should be no options.
    int duration = TimeRange.WHOLE_DAY.duration() + 1;
    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), duration);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = Arrays.asList();

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void eventSplitsRestriction() {
    // The event should split the day into two options (before and after the event).
    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES), Arrays.asList(PERSON_A)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
        TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void everyAttendeeIsConsidered() {
    // Have each person have different events. We should see two options because each person has
    // split the restricted times.
    //
    // Events  :       |--A--|     |--B--|
    // Day     : |-----------------------------|
    // Options : |--1--|     |--2--|     |--3--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES), Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES), Arrays.asList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
        TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false),
        TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void everyRequiredAttendeeIsConsidered() {
    // Same scenario as with everyAttendeeIsConsidered() except that, now, there is one optional attendee who is busy the whole day.
    // We should get the same 3 available time slots as above because the optional attendee is never free.

    Collection<Event> events = new ArrayList<>();
    events.add(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES), Arrays.asList(PERSON_A)));
    events.add(
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES), Arrays.asList(PERSON_B)));
    events.add(new Event("Event 3", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, true),
        Arrays.asList(PERSON_C)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_C);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
        TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false),
        TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void everyRequiredAndOptionalAttendeeAreConsidered() {
    // Similar scenarios than everyAttendeeIsConsidered() and everyRequiredAttendeeIsConsidered() except that, now, the optional attendee has
    // common availabilities with the required attendees. We should see 2 options: the times where both required and optional attendees are available.
    //
    // Events  :       |--A--|--C--|--B--|
    // Day     : |-----------------------------|
    // Options : |--1--|                 |--2--|

    Collection<Event> events = new ArrayList<>();
    events.add(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES), Arrays.asList(PERSON_A)));
    events.add(
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES), Arrays.asList(PERSON_B)));
    events.add(
        new Event("Event 3", TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES), Arrays.asList(PERSON_C)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_C);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
        TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void everyOptionalAttendeeIsConsidered() {
    // Have an event for each optional attendees, no overlap. There is no mandatory attendees.
    // We should, similarly as with required attendees, see 3 options.
    //
    // Events  :       |--A--|     |--B--|
    // Day     : |-----------------------------|
    // Options : |--1--|     |--2--|     |--3--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES), Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES), Arrays.asList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(NO_ATTENDEES, DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_A);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
        TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false),
        TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void overlappingEvents() {
    // Have an event for each person, but have their events overlap. We should only see two options.
    //
    // Events  :       |--A--|
    //                     |--B--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_60_MINUTES), Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_60_MINUTES), Arrays.asList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
        TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void overlappingEventsWithOptional() {
    // Have an event for each person (one optional and one mandatory attendee), but have their events overlap. We should only see two options, similar as for overlappingEvents() 
    //
    // Events  :       |--A--|
    //                     |--B--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|
    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_60_MINUTES), Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_60_MINUTES), Arrays.asList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
        TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void nestedEvents() {
    // Have an event for each person, but have one person's event fully contain another's event. We
    // should see two options.
    //
    // Events  :       |----A----|
    //                   |--B--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_90_MINUTES), Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES), Arrays.asList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
        TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void doubleBookedPeople() {
    // Have one person, but have them registered to attend two events at the same time.
    //
    // Events  :       |----A----|
    //                     |--A--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_60_MINUTES), Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES), Arrays.asList(PERSON_A)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
        TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void justEnoughRoom() {
    // Have one person, but make it so that there is just enough room at one point in the day to
    // have the meeting.
    //
    // Events  : |--A--|     |----A----|
    // Day     : |---------------------|
    // Options :       |-----|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true), Arrays.asList(PERSON_A)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void justEnoughRoomWithOptionalAttendee() {
    // Similar scenario as justEnoughRoom() except that, now, an optional attendee has an event during the
    // only available time of the required attendee(s). We should still see the same available time slot
    // as above since the optional attendee would be ignored. 
    //
    // Events  : |--A--|-B-|   |----A----|
    // Day     : |-----------------------|
    // Options :       |-------|

    Collection<Event> events = new ArrayList<>();
    events.add(new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
        Arrays.asList(PERSON_A)));
    events.add(
        new Event("Event 2", TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true), Arrays.asList(PERSON_A)));
    events.add(
        new Event("Event 3", TimeRange.fromStartDuration(TIME_0830AM, DURATION_15_MINUTES), Arrays.asList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void justEnoughRoomOptionalAndRequired() {
    // Similar scenario as justEnoughRoom() except that, now, the second event is of an optional attendee. We should still see the same available time slot
    // as for justEnoughRoom() since we want their common available times. 
    //
    // Events  : |--A--|    |----B----|
    // Day     : |--------------------|
    // Options :       |----|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true), Arrays.asList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void justEnoughRoomOptionalAndRequiredOpposite() {
    // Same scenario as justEnoughRoomOptionalAndRequired() but the other way around (first the optional attendee then the mandatory one). We should still see the same available time slot
    // since we want their common available times. 
    //
    // Events  : |--B--|    |----A----|
    // Day     : |--------------------|
    // Options :       |----|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            Arrays.asList(PERSON_B)),
        new Event("Event 2", TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true), Arrays.asList(PERSON_A)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void ignoresPeopleNotAttending() {
    // Add an event, but make the only attendee someone different from the person looking to book
    // a meeting. This event should not affect the booking.
    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES), Arrays.asList(PERSON_A)));
    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noConflicts() {
    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void notEnoughRoom() {
    // Have one person, but make it so that there is not enough room at any point in the day to
    // have the meeting.
    //
    // Events  : |--A-----| |-----A----|
    // Day     : |---------------------|
    // Options :

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true), Arrays.asList(PERSON_A)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_60_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList();

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void notEnoughRoomOptionalAttendees() {
    // Have two optional attendees, but make it so that there is not enough room at any point in the day to
    // have the meeting. There is no mandatory attendees. 
    // We should see the whole day as available, as if there were no attendees. 
    //
    // Events  : |--A-----| |-----B----|
    // Day     : |---------------------|
    // Options :

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true), Arrays.asList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(NO_ATTENDEES, DURATION_60_MINUTES);
    request.addOptionalAttendee(PERSON_A);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }
}
