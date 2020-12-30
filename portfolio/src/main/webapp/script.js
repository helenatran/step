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

document.addEventListener('DOMContentLoaded', async function() {
  initialiseMaterializeElements();
  loadNavBarFooter();
  hideCommentSection();
  hideCommentForm();
});

/** Initialise the slider (in index.html) and the material box (in photoGallery.html) */
function initialiseMaterializeElements() {
  const elems_one = document.querySelectorAll('.slider');
  const instances_one = M.Slider.init(elems_one);
  const elems_two = document.querySelectorAll('.materialboxed');
  const instances_two = M.Materialbox.init(elems_two);
}

/** Load the navbar and footer. */
function loadNavBarFooter() {
  document.getElementById('navbar-container').innerHTML='<object type="text/html" data="navBar.html" width="100%" height="73"></object>'
  document.getElementById('footer-container').innerHTML='<object type="text/html" data="footer.html" width="100%" height="115"></object>'
}

/** Hide comment section if there is no comment. */
async function hideCommentSection() {
  const commentSectionContainer = document.getElementById('comments-section');
  const comments = await getAllComments();
  if (comments.length === 0) {
    commentSectionContainer.style.display = 'none';
  }
  else {
    commentSectionContainer.style.display = 'block';
  }
}

/** Hide comment form if the user is not logged-in. */
async function hideCommentForm() {
  const response = await fetch('/login', {method: 'POST'});
  const isUserLoggedIn = await response.json();

  const commentFormContainer = document.getElementById('comment-form');
  const loginRequestContainer = document.getElementById('login-request');
  if (isUserLoggedIn) {
    commentFormContainer.style.display = 'block';
    loginRequestContainer.style.display = 'none';
  }
  else {
    commentFormContainer.style.display = 'none';
    loginRequestContainer.style.display = 'block';
  }
}

/** Adds a random greeting to the page. */
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

/** Display the 'About Me' section. */
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
    const limitNumber = document.getElementById('commentsNumber').value;
    const response = await fetch('/data?limit=' + limitNumber);
    const comments = await response.json();

    commentListElement = document.getElementById('comments-list');
    commentListElement.innerHTML = '';
    comments.forEach((comment) => {
        commentListElement.appendChild(createCommentElement(comment));
    })
}

/** Creates an element that represents a comment, including its delete button. */
function createCommentElement(comment) {
  const commentElement = document.createElement('div');
  commentElement.className = 'card horizontal';

  const subDivElement = document.createElement('div');
  subDivElement.className= 'card-stacked';

  const lastDivElement = document.createElement('div');
  lastDivElement.className = 'card-content';

  const usernameElement = document.createElement('p');
  usernameElement.innerText = "Username: " + comment.username;

  const emailElement = document.createElement('p');
  emailElement.innerText = 'Email: ' + comment.email;

  const commentTextElement = document.createElement('p');
  commentTextElement.innerText = 'Comment: ' + comment.commentText + ' (' + comment.sentimentScore.toFixed(2) + ')';

  const deleteButtonElement = document.createElement('a');
  deleteButtonElement.className = 'waves-effect waves-light btn-small pink lighten-3';
  deleteButtonElement.setAttribute('style', 'float:right; margin-top:-50px;')
  deleteButtonElement.innerHTML = '<i class="material-icons">delete</i>';
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(comment);

    // Remove the task from the DOM.
    commentElement.remove()
  });

  commentElement.appendChild(subDivElement);
  subDivElement.appendChild(lastDivElement);
  lastDivElement.appendChild(usernameElement);
  lastDivElement.appendChild(emailElement);
  lastDivElement.appendChild(commentTextElement);
  lastDivElement.appendChild(deleteButtonElement);

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
  const comments = await getAllComments();
  for (i = 0; i < comments.length; i++) {
    deleteComment(comments[i]);
  }
  await loadAfterDelete();
  const commentSectionContainer = document.getElementById('comments-section');
  commentSectionContainer.style.display = 'none';
}

/** Load an empty comment section after all comments have been deleted. */
async function loadAfterDelete() {
  const response = await fetch('/data');
  const comments = await response.json();
  commentListElement = document.getElementById('comments-list');
  commentListElement.innerHTML = '';
}

/** Get all comments. */
async function getAllComments() {
  const response = await fetch('/data');
  const comments = await response.json();
  return comments;
}

/** Creates my map and add it to the page. */
async function createMyMap() {
  const response = await fetch('/map-data');
  const mapMarkers = await response.json();

  const map = new google.maps.Map(
      document.getElementById('my-map'),
      {center: {lat: -33.8783, lng: 151.1850}, zoom: 13});
  
  mapMarkers.forEach((marker) => {
    addMarker(map, marker.lat, marker.lng, marker.title, marker.description);
  })
}

// Initialise the map for the user to add markers
let yourMap;

/** Creates a map for the other users and add it to the page. */
async function createYourMap() {
  yourMap = new google.maps.Map(
    document.getElementById('your-map'),
    {center: {lat: -33.8783, lng: 151.1850}, zoom: 13});

  // When the user clicks in the map, show a marker with a text box the user can edit.
  yourMap.addListener('click', (event) => {
    createMarkerForEdit(event.latLng.lat(), event.latLng.lng());
  });
  
  const response = await fetch('/your-map-data');
  const mapMarkers = await response.json();

  mapMarkers.forEach((marker) => {
    addMarker(yourMap, marker.lat, marker.lng, marker.title, marker.description);
  });
}

/** 
 * Display the map which allows users to add marker only if they are logged in.
 * If they are not, prompt them to log in. 
 */
async function displayYourMapIfLoggedIn() {
  const response = await fetch('/login', {method: 'POST'});
  const isUserLoggedIn = await response.json();

  const mapLoginRequestContainer = document.getElementById('map-login-request');
  const yourMapContainer = document.getElementById('your-map');
  if (isUserLoggedIn) {
    yourMapContainer.style.display = 'block';
    mapLoginRequestContainer.style.display = 'none';
  }
  else {
    yourMapContainer.style.display = 'none';
    mapLoginRequestContainer.style.display = 'block';
  }
}

/** Load both maps. */
function loadMaps() {
  createMyMap();
  createYourMap();
  displayYourMapIfLoggedIn();
}

/** Adds a marker that shows an info window when clicked. */
function addMarker(map, lat, lng, title, description) {
  const marker = new google.maps.Marker(
      {position: {lat: lat, lng: lng}, map: map, title: title});
  
  const infoWindowContent = createInfoWindow(title, description);
  const infoWindow = new google.maps.InfoWindow({content: infoWindowContent});
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  });
}

/** Creates an element that represents the content of an info window. */
function createInfoWindow(title, description) {
  const divElement = document.createElement('div');

  const titleElement = document.createElement('h6');
  titleElement.innerText = title;

  const descriptionElement = document.createElement('p');
  descriptionElement.innerText = description;

  divElement.appendChild(titleElement);
  divElement.appendChild(descriptionElement);

  return divElement;

}

/* Editable marker that displays when a user clicks in the map. */
let editMarker;

/** Creates a marker that shows a textbox the user can edit. */
function createMarkerForEdit(lat, lng) {
  // If we're already showing an editable marker, then remove it.
  if (editMarker) {
    editMarker.setMap(null);
  }

  editMarker =
      new google.maps.Marker({position: {lat: lat, lng: lng}, map: yourMap});

  const infoWindow =
      new google.maps.InfoWindow({content: buildInfoWindowInput(lat, lng)});

  // When the user closes the editable info window, remove the marker.
  google.maps.event.addListener(infoWindow, 'closeclick', () => {
    editMarker.setMap(null);
  });

  infoWindow.open(yourMap, editMarker);
}

/**
 * Builds and returns HTML elements that show an editable textbox and a submit
 * button.
 */
function buildInfoWindowInput(lat, lng) {
  const textBoxElement = document.createElement('textarea');
  const buttonElement = document.createElement('button');
  buttonElement.appendChild(document.createTextNode('Submit'));
  
  buttonElement.onclick = () => {
    postMarker(lat, lng, textBoxElement.value);
    addMarker(yourMap, lat, lng, '', textBoxElement.value);
    editMarker.setMap(null);
  };

  const containerDiv = document.createElement('div');
  containerDiv.appendChild(textBoxElement);
  containerDiv.appendChild(document.createElement('br'));
  containerDiv.appendChild(buttonElement);

  return containerDiv;
}

/** Sends a marker to the backend for saving. */
function postMarker(lat, lng, description) {
  const params = new URLSearchParams();
  params.append('lat', lat);
  params.append('lng', lng);
  params.append('description', description);

  fetch('/your-map-data', {method: 'POST', body: params});
}

google.charts.load('current', {'packages':['timeline']});
google.charts.setOnLoadCallback(drawChart);

/** Creates a chart and adds it to the page. */
function drawChart() {
  var container = document.getElementById('chart-container');
  var chart = new google.visualization.Timeline(container);
  var dataTable = new google.visualization.DataTable();

  dataTable.addColumn({ type: 'string', id: 'ProjectNumber' });
  dataTable.addColumn({ type: 'string', id: 'ProjectName' });
  dataTable.addColumn({ type: 'date', id: 'Start' });
  dataTable.addColumn({ type: 'date', id: 'End' });
  dataTable.addRows([
    [ '1', 'Movie Kiosk Software',          new Date(2018, 2, 1),  new Date(2018, 5, 1) ],
    [ '2', 'Directed Graph Structure',      new Date(2019, 2, 1),  new Date(2019, 5, 1) ],
    [ '3', 'E-shop - Computer devices',     new Date(2019, 6, 1),  new Date(2019, 9, 1) ],
    [ '4', 'Electrical Forecast Database',  new Date(2020, 2, 1),  new Date(2020, 5, 1) ],
    [ '5', 'E-Library System',              new Date(2020, 6, 1),  new Date(2020, 9, 1) ],
    [ '6', 'IOU - favour exchange',         new Date(2020, 6, 1),  new Date(2020, 9, 1) ]]);

  var options = {
    timeline: { showRowLabels: false }
  };

  chart.draw(dataTable, options);
}
