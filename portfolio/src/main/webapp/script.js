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
    text = "<h3>My hobbies:</h3> <ul>";
    for (i = 0; i < hobbies.length; i++) {
        text += "<li>" + hobbies[i] + "</li>"
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
    const lovesContainer = document.getElementById('about-me-container');
    text = "<h3>What I love: </h3> <ul>";
    for (i = 0; i < loves.length; i++) {
        text += "<li>" + loves[i] + "</li>"
    }
    text += "</ul>"
    lovesContainer.innerHTML = text;
}

/**
 * Display what I hate
 */
function getHates() {
    const hates = [
        'mess (so I\'m usually quite organised!)'
    ];

    //Add them to the page
    const hatesContainer = document.getElementById('about-me-container');
    text = "<h3>What I hate: </h3> <ul>";
    for (i = 0; i < hates.length; i++) {
        text += "<li>" + hates[i] + "</li>"
    }
    text += "</ul>"
    hatesContainer.innerHTML = text;
}