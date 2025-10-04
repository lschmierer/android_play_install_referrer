> **Deprecation!**
> 
> This project was forked and is maintained by [ChunkyTofuStudios](https://github.com/ChunkyTofuStudios).
> Please use their project instead https://github.com/ChunkyTofuStudios/play_install_referrer

# android_play_install_referrer
[![pub package](https://img.shields.io/pub/v/android_play_install_referrer.svg)](https://pub.dartlang.org/packages/android_play_install_referrer)

A Flutter plugin for the Android Play Install Referrer API.
The plugins throws an exception on iOS and on Android if Google Play Services are not available.

## Usage

Get Google Play Install Referrer Details:

```Dart
ReferrerDetails referrerDetails = await AndroidPlayInstallReferrer.installReferrer;
```

## Passing Data Through Google Play Stroe Link

Pass Data With ```referrer``` URL query parameter:

Assuming, links could look like this (replace ```com.example.myapp``` with real application ID):

```https://play.google.com/store/apps/details?id=com.example.myapp&referrer=someKey%3DsomeValue```

##  Explanation

URL Components: ⤵️

Base URL for the app’s Google Play Store page  ```https://play.google.com/store/apps/details?id=```

Application ID:  ```com.example.myapp```

URL parameter:    ```&referrer=```

Custom Key-Value Data: Allows custom key-value pairs, such as ```someKey``` as the key and ```someValue``` as the value (```%3D``` represents ```=``` in URL encoding):

```someKey%3DsomeValue```

##  Testing Referral

To test a referral link, release a version of the app on the Play Store. Referral links cannot be tracked through a debug APK, but they will work in testing phases (internal, open, closed) and in production.

Anyone who installs the app by clicking the referral link can be tracked.

For more information see https://developer.android.com/google/play/installreferrer/library.html
