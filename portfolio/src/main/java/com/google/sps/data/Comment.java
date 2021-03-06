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

/** An item on a comment section. */
public final class Comment {

  private final long id;
  private final String username;
  private final String email;
  private final String commentText;
  private final double sentimentScore;
  private final long timestamp;

  /** Create a new comment. 
   *  @param id             the id the of the comment.
   *  @param username       the username of the person adding the comment.
   *  @param email          the email of the logged-in user who is posting the comment.
   *  @param sentimentScore the score of the comment from sentiment analysis. 
   *  @param commentText    the text of the comment.
   *  @param timestamp      the timestamp at which the comment has been created.
  */
  public Comment(long id, String username, String email, String commentText, double sentimentScore, long timestamp) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.commentText = commentText;
    this.sentimentScore = sentimentScore;
    this.timestamp = timestamp;
  }
}
