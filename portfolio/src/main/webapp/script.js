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

document.addEventListener('DOMContentLoaded', function() {
  // Initialise the slider (index.html) and the material box (photoGallery.html)
  elems_one = document.querySelectorAll('.slider');
  instances_one = M.Slider.init(elems_one);
  elems_two = document.querySelectorAll('.materialboxed');
  instances_two = M.Materialbox.init(elems_two);

  //Load Navigation Bar and Footer when loading page
  document.getElementById('navbar-container').innerHTML='<object type="text/html" data="navBar.html" width="100%" height="73"></object>'
  document.getElementById('footer-container').innerHTML='<object type="text/html" data="footer.html" width="100%" height="115"></object>'
});

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
    ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

/**
 * Add a random fact about me to the page
 */
function addRandomFact() {
  const facts = [
    'I am a foodie',
    'I have a collection of Starbucks mugs from around the world',
    'I have a collection of "I LOVE" T-shirt from around the world',
    'I have travelled to over 15 different countries',
    'I have never broken anything on my body',
    'I used to do Karate when I was younger',
    'I watched all the Harry Potters in 2019 because I never watched them before (I know, shame on me!)'
  ];

  //Pick a random fact
  const fact = facts[Math.floor(Math.random() * facts.length)];

  //Add it to the page
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

/**
 * Display the 'About Me' section
 */
function getAboutMe(aboutMeId) {
  if (aboutMeId === 'hobbies') {
    elemId = 'hobbies-container';
  }
  if (aboutMeId === 'loves') {
    elemId = 'loves-container';
  }
  if (aboutMeId === 'hates') {
    elemId = 'hates-container';
  }

  const aboutMeContainer = document.getElementById('about-me-container');
  const subContainer = document.getElementById(elemId);

  const hobbiesContainer = document.getElementById('hobbies-container');
  const lovesContainer = document.getElementById('loves-container');
  const hatesContainer = document.getElementById('hates-container');

  if (aboutMeContainer.style.display === 'none') {
    aboutMeContainer.style.display = 'block';
    subContainer.style.display = 'block';
  }
  else if(aboutMeContainer.style.display !== 'none' && subContainer.style.display === 'none') {
    subContainer.style.display = 'block';
      if (aboutMeId === 'hobbies') {
        lovesContainer.style.display = 'none';
        hatesContainer.style.display = 'none';
      }
      if (aboutMeId === 'loves') {
        hobbiesContainer.style.display = 'none';
        hatesContainer.style.display = 'none';
      }
      if (aboutMeId === 'hates') {
        hobbiesContainer.style.display = 'none';
        lovesContainer.style.display = 'none';
      }
  }
  else {
    aboutMeContainer.style.display = 'none';
    subContainer.style.display = 'none';
  }
}

/** Fetches comments from the server and add them to the DOM. */
async function loadComments() {
    limitNo = document.getElementById('commentsNo').value;
    const response = await fetch('/data?limit=' + limitNo);
    const comments = await response.json();

    commentListElement = document.getElementById('comments-list');
    commentListElement.innerHTML = '';
    comments.forEach((comment) => {
        commentListElement.appendChild(createCommentElement(comment));
    })
}

/** Creates an element that represents a comment, including its delete button. */
function createCommentElement(comment) {
  const commentElement = document.createElement('li');
  commentElement.className = 'comment';

  const usernameElement = document.createElement('span');
  usernameElement.innerText = "Username: " + comment.username;

  const commentTextElement = document.createElement('span');
  commentTextElement.innerText = "; Comment: " + comment.commentText + " ";

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'Delete';

  deleteButtonElement.addEventListener('click', () => {
    deleteComment(comment);

    // Remove the task from the DOM.
    commentElement.remove();
  });

  commentElement.appendChild(usernameElement);
  commentElement.appendChild(commentTextElement);
  commentElement.appendChild(deleteButtonElement);
  return commentElement;
}

/** Tells the server to delete the comment. */
function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  fetch('/delete-data', {method: 'POST', body: params});
}

/** Tells the server to delete all the comments. */
async function deleteAllComments() {
  const response = await fetch('/data');
  const comments = await response.json();
  for (i = 0; i < comments.length; i++) {
    deleteComment(comments[i]);
  }
  await loadComments();
}
