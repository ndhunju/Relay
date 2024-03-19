# Relay App

Android app to relay calls (WIP) and messages from child device to parent device. 
This comes in handy when you are traveling to a different country but want 
to be able to receive calls and messages from your country's number.

This app uses best practices for Android and uses the following libraries:

✅ **Firestore Database**: For temporarily storing data in cloud </br>
✅ **Firebase Analytics**: For logging key app events and logs for better debugging</br>
✅ **Firebase Crashlytics**: For getting crash reports</br>
✅ **Firebase Messaging**: For sending push notification to the user</br>
✅ **Jetpack Compose**: For building modern and declarative UIs.</br>
✅ **WorkManager**: For syncing server data to local database periodically.</br>
✅ **DateStore**: For storing simple key value persistently on device.</br>
✅ **Barcode Generator**: For generating QR codes.</br>
✅ **Barcode Scanner**: For scanning QR codes.</br>
✅ **Gson**: For serializing and deserializing.</br>
✅ **Room**: To store persistent data offline</br>
✅ **Dagger**: For dependency injection.</br>
✅ **JUnit**: For unit testing.</br>
✅ **Mockito**: To mock behavior during testing.</br>

### Icons

<table>
  <tr>
    <th>App Icon</th>
    <th>Animated App Icon</th>
  </tr>
  <tr>
    <td align="center"><img src="graphics/icons/ic_app/ic_app.svg" width="135" height="108" alt="app icon"></td>
    <td align="center"><img src="graphics/icons/ic_app/ic_app_animated.gif" width="130" height="130" alt="animated icon"></td>
  </tr>
</table>

### Screens
Scroll horizontally to see more ➡️

<table>
  <tr>
    <th>Splash Screen</th>
    <th>Welcome Screen</th>
    <th>Account Screen</th>
    <th>Side Navigation</th>
    <th>Messages Screen</th>
  </tr>
  <tr>
    <td><img src="graphics/screens/screen_splash.gif" alt="gif of splash screen"   width="256" height="512"></td>
    <td><img src="graphics/screens/screen_welcome.gif" alt="gif of welcome screen" width="256" height="512"></td>
    <td><img src="graphics/screens/screen_account.gif" alt="gif of account screen" width="256" height="512"></td>
    <td><img src="graphics/screens/screen_side_navigation.gif" alt="gif of side navigation" width="256" height="512"></td>
    <td><img src="graphics/screens/screen_messages.gif" alt="gif of messages screen" width="256" height="512"></td>
    
  </tr>
  <tr>
    <td width="256"><p>Splash Screen with animated app logo.</p></td>
    <td width="256"><p>Welcome Screen with animated button.</p></td>
    <td width="256"><p>Account Screen that respects dynamic theme set by the user and handles error.</p></td>
    <td width="256"><p>Side navigation.</p></td>
    <td width="256"><p>Messages Screen that shows all messages in the thread.</p></td>
  </tr>
</table>