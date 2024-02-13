# Relay App

Android app to relay calls and messages from child device to parent device. 
This comes in handy when you are traveling to a different country but want 
to be able to receive calls and messages from your country's number.

This app uses best practices for Android and uses the following libraries:

- **Firestore Database**: For temporarily storing data in cloud
- **Firesbase Analytics**: For logging key app events and logs for better debugging
- **Jetpack Compose**: For building modern and declarative UIs.
- **WorkManager**: For syncing server data to local database periodically.
- **DateStore**: For storing simple key value persistently on device.
- **Gson**: For serializing and deserializing.
- **Room**: To store persistent data offline
- **Dagger**: For dependency injection.
- **JUnit**: For unit testing.

<table>
<tr>
    <th>App Icon</th>
    <th>App Icon Grid</th>
  </tr>
  <tr>
    <td><img src="graphics/icons/ic_app/ic_app.svg" width="135" height="108" alt=""></td>
    <td><img src="graphics/icons/ic_app/ic_app_with_grids.svg" width="135" height="108" alt=""></td>
  </tr>
  <tr>
    <th>Splash Screen</th>
    <th>Account Screen</th>
  </tr>
  <tr>
    <td>
        <video height="342" width="228" autoplay="autoplay">
            <source src="graphics/screens/screen_splash.mp4" type="video/mp4">
        </video>
    <td>
        <video height="342" width="228" autoplay="autoplay">
            <source src="graphics/screens/screen_account.mp4" type="video/mp4">
        </video>
    </td>
  </tr>
<tr>
    <td><p>Splash Screen with animated app logo.</p></td>
    <td><p>Account Screen that respects dynamic theme set by the user.</p></td>
  </tr>
</table>