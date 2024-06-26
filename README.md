# LiveLook Android MVVM Location Tracker App

## Overview

The Android MVVM Location Tracker App is a feature-rich application that leverages the MVVM architecture pattern, a repository, and various Android features to provide live location tracking. The app continuously logs the device's coordinates, displays them on a map, and sends notifications for location updates.

## Screenshots

<img src="WhatsApp%20Image%202024-01-24%20at%2015.52.26%20(1).jpeg" alt="Location Notification" width="200" height="200">
*Location Notification: The app provides real-time location updates in the notification bar.*

<img src="WhatsApp%20Image%202024-01-24%20at%2015.52.26.jpeg" alt="App Screen" width="300" height="600">
*App Screen: The app includes a live map displaying the current location.*

<img src="WhatsApp%20Image%202024-04-10%20at%2015.39.44.jpeg" alt="App Screen" width="300" height="600">
*App Screen: The app includes a live camera displaying the current location.*


## Features

- **MVVM Architecture:** Utilizes the Model-View-ViewModel architecture pattern for a clean and organized codebase.

- **Repository Pattern:** Manages data retrieval and storage using a repository pattern.

- **Fine Location Access:** Requests and utilizes fine location access for accurate location tracking.

- **Foreground Service:** Implements a foreground service for continuous location tracking even when the app is in the background.

- **Logging Coordinates:** Logs live location coordinates to the Logcat for debugging and monitoring.

- **File Permissions:** Manages file permissions for secure storage of location data.

- **Map View:** Integrates Google Maps SDK for Android to display live locations on a map.

- **Notifications:** Sends notifications for real-time location updates.

## Setup

1. **Clone the repository:**

    ```bash
    git clone https://github.com/SushmitSingh/LIveLook.git
    ```

2. **Open the project in Android Studio.**

3. **Build and run the app on your Android device or emulator.**

## Dependencies

- [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [LiveData](https://developer.android.com/topic/libraries/architecture/livedata)
- [Google Maps SDK](https://developers.google.com/maps/documentation/android-sdk/overview)
- [Foreground Service](https://developer.android.com/guide/components/services#Foreground)

## Usage

The app automatically starts tracking your location when launched. You can view live location updates on the map and receive notifications for each location update.

## Acknowledgments

- Special thanks to [Google](https://developers.google.com/maps/documentation/android-sdk/overview) for providing the Maps SDK.
- Inspired by the MVVM architecture and Android best practices.

Feel free to contribute or report issues! Happy coding!
