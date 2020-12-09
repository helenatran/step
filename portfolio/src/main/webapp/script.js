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
    elems = document.querySelectorAll('.slider');
    instances = M.Slider.init(elems);
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
        'I have a collection of Starbucks mugs',
        'I have a collection of "I LOVE" T-shirt from around the world',
        'I have travelled to over 15 different countries'
    ];

    //Pick a random fact
    const fact = facts[Math.floor(Math.random() * facts.length)];

    //Add it to the page
    const factContainer = document.getElementById('fact-container');
    factContainer.innerText = fact;
}

isHobbiesDisplayed = false;
isLovesDisplayed = false;
isHatesDisplayed = false;

/**
 * Display my hobbies
 */
function getHobbies() {
    const hobbies = [
        'photography',
        'dance',
        'video games',
        'gym & yoga',
        'cooking'
    ];

    //Add them to the page
    const aboutMeContainer = document.getElementById('about-me-container');
    if (aboutMeContainer.style.display === 'none') {
        aboutMeContainer.style.display = 'block';
        isHobbiesDisplayed = true;
    }
    else if(aboutMeContainer.style.display !== 'none' && (isLovesDisplayed === true || isHatesDisplayed === true)) {
        aboutMeContainer.style.display = 'block';
        isHobbiesDisplayed = true;
        isLovesDisplayed = false;
        isHatesDisplayed = false;
    }
    else {
        aboutMeContainer.style.display = 'none';
        isHobbiesDisplayed = false;
    }
    text = text = "<ul class=\"collection with-header z-depth-3\"><li class=\"collection-header center purple lighten-4 white-text\"><h4>My Hobbies</h4></li>";;
    for (i = 0; i < hobbies.length; i++) {
        text += "<li class=\"collection-item grey lighten-4\">" + hobbies[i] + "</li>"
    }
    text += "</ul>"
    aboutMeContainer.innerHTML = text;
    
}

/**
 * Display what I love
 */
function getLoves() {
    const loves = [
        'dogs (especially corgis, and I have one!)',
        'movies/series (favourite genre: romantic & comedy)',
        'books (favourite genre: romance & self-help)',
        'fashion',
        'my planner (keep me organised!)'
    ];

    //Add them to the page
    const aboutMeContainer = document.getElementById('about-me-container');
    if (aboutMeContainer.style.display === 'none') {
        aboutMeContainer.style.display = 'block';
        isLovesDisplayed = true;
    }
    else if(aboutMeContainer.style.display !== 'none' && (isHobbiesDisplayed === true || isHatesDisplayed === true)) {
        aboutMeContainer.style.display = 'block';
        isLovesDisplayed = true;
        isHobbiesDisplayed = false;
        isHatesDisplayed = false;
    }
    else {
        aboutMeContainer.style.display = 'none';
        isLovesDisplayed = false;
    }
    text = text = "<ul class=\"collection with-header z-depth-3\"><li class=\"collection-header center purple lighten-4 white-text\"><h4>What I love</h4></li>";;
    for (i = 0; i < loves.length; i++) {
        text += "<li class=\"collection-item grey lighten-4\">" + loves[i] + "</li>"
    }
    text += "</ul>"
    aboutMeContainer.innerHTML = text;
}

/**
 * Display what I hate
 */
function getHates() {
    const hates = [
        'mess (so I\'m usually quite organised!)'
    ];

    //Add them to the page
    const aboutMeContainer = document.getElementById('about-me-container');
    if (aboutMeContainer.style.display === 'none') {
        aboutMeContainer.style.display = 'block';
        isHatesDisplayed = true;
    }
    else if(aboutMeContainer.style.display !== 'none' && (isHobbiesDisplayed === true || isLovesDisplayed === true)) {
        aboutMeContainer.style.display = 'block';
        isHatesDisplayed = true;
        isHobbiesDisplayed = false;
        isLovesDisplayed = false;
    }
    else {
        aboutMeContainer.style.display = 'none';
        isHatesDisplayed = false;
    }
    text = text = "<ul class=\"collection with-header z-depth-3\"><li class=\"collection-header center purple lighten-4 white-text\"><h4>What I hate</h4></li>";;
    for (i = 0; i < hates.length; i++) {
        text += "<li class=\"collection-item grey lighten-4\">" + hates[i] + "</li>"
    }
    text += "</ul>"
    aboutMeContainer.innerHTML = text;
}