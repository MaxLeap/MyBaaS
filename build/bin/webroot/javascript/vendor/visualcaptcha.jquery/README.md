visualCaptcha-frontend-jquery
=============================

jQuery plug-in for the visualCaptcha front-end core package.


## Installation with Bower

```
bower install visualcaptcha.jquery
```


## Usage

### Initialization 

1. Include jQuery library and jQuery version of the visualCaptcha front-end library into the HTML page:

    ```html
    <script src="/path_to/jquery.min.js"></script>
    <script src="/path_to/visualcaptcha.jquery.js"></script>
    ```

2. Create a visualCaptcha container into the HTML page:

    ```html
    <div id="sample-captcha"></div>
    ```

3. Initialize captcha with the `$( element ).visualCaptcha( options )` jQuery function that returns _jQuery object_ within the _visualCaptcha object_. It use two parameters: `element` is a selector of a container for the visualCaptcha, `options` is a JSON object of the visualCaptcha options:

    ```javascript
    var el = $( '#sample-captcha' ).visualCaptcha( {
        imgPath: 'img/',
        captcha: { numberOfImages: 5 }
    } );

    // use next code for getting captcha object
    var capthca = el.data( 'captcha' );
    ```

### VisualCaptcha options

JSON object of the visualCaptcha options can contain next parameters:

- `imgPath` (default: `'/'`) — path to the next interface icons:
    - `accessibility.png`,
    - `accessibility@2x.png`,
    - `refresh.png`,
    - `refresh@2x.png`;

- `language` — object of the text values using for localization the visualCaptcha interface, default:
    ```
    {
        accessibilityAlt: 'Sound icon',
        accessibilityTitle: 'Accessibility option: listen to a question and answer it!',
        accessibilityDescription: 'Type below the <strong>answer</strong> to what you hear. Numbers or words:',
        explanation: 'Click or touch the <strong>ANSWER</strong>',
        refreshAlt: 'Refresh/reload icon',
        refreshTitle: 'Refresh/reload: get new images and accessibility option!'
    }
    ```

- `captcha` — object of the visualCaptcha core options :
    - `request` (default: `xhrRequest`) — function for sending request;
    - `url` (default: `'http://localhost:8282'`) — url for back-end;
    <!-- !FIXME - `path` (default: `''`) — is the url prefix; -->
    <!-- !FIXME - `autoRefresh` (default: `true`) — if it is `true` it will load the data when it's constructed; -->
    - `numberOfImages` (default: `6`) — number of generated images for visualCaptcha;
    - `namespaceFieldName` (default: `'namespace'`) — the name of the parameter sent to the server for the namespace;
    — `namespace` — the value of the parameter sent to the server for the namespace, if it's not setted up, no namespace will be sent;
    - `randomParam` (default: `'r'`) — name of random value parameter which is for disable the cache;
    - `routes` — object with next endpoint routes:
        - `start` (default: `'/start'`) — route to generate common data (image field name, image name, image values and audio field name);
        - `image` (default: `'/image'`) — route to get generated image file by index;
        - `audio` (default: `'/audio'`) — route to get generated audio file.

### VisualCaptcha object methods

All next methods are available from _VisualCaptcha object_ that will be returned by `$( element ).visualCaptcha( options ).data( 'captcha' )` funciton (as it was described above, in “Initialization”, step 3).

- `audioFieldName()` — returns field name of accessibility (audio) captcha;
- `audioUrl()` — returns URL of audio file;
- `getCaptchaData()` — returns captcha data object:
    - `valid` — is `true` if image or audio field is filled or `false` if nothing filled;
    - `name` — is field name of filled input;
    - `value` — is value of filled input;
- `hasLoaded()` — returns `true` if VisualCaptcha is loaded, else returns `false`;
- `imageFieldName()` — returns field name of image captcha;
- `imageName()` — name of the image object for pass visualCaptcha correct;
- `imageUrl( index )` — returns URL of image file by index, index is number;
- `imageValue( index )` — returns value of image file by index, index is number;
- `isLoading()` — returns `true` if VisualCaptcha is loading, else returns `false`;
- `isRetina()` — returns `true` for devises with retina display, else returns `false`;
- `numberOfImages()` — returns number of generated images;
- `refresh()` — reloads visual captcha, sends new request to the back-end;
- `supportsAudio()` — returns `true` if browser supports HTML 5 Audio, else returns `false`;

### Initialization of multiple captchas on a page

There are two fields: `namespace` and `namespaceFieldName` for creating multiple captchas.
The `namespace` option can be loaded from the `data-namespace` attribute:
```html
<form id='login-form'>
    <!-- ... -->
    <div class="captcha" data-namespace="login"></div>
    <!-- ... -->
</form>

<form id='search-form'>
    <!-- ... -->
    <div class="captcha" data-namespace="search"></div>
    <!-- ... -->
</form>
```

And the `namespaceFieldName` option can be loaded from the captcha options:
```javascript
$('.captcha').visualCaptcha({
    captcha: {
        namespaceFieldName: 'myFieldName'
    }
});
```

Such configuration will create a hidden field in each form with a captcha
with the field name of `namespaceFieldName` and the field value of `namespace`
for initialize multiple captchas.

## License

The MIT License (MIT)

Copyright (c) 2014 emotionLoop

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
