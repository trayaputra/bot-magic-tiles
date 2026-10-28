# Magic Tiles 3 Bot (Android / Kotlin)

Starter project for an Android screen-detection auto-tap helper. It includes:
- MediaProjection screen capture with explicit Android consent
- AccessibilityService gesture taps
- Floating ON/OFF and STOP controls
- Adjustable tap interval
- Basic four-lane dark-pixel detector

## Build

Open this folder in Android Studio (JDK 17, Android SDK 35), sync Gradle, then Build > Build APK(s).

To build in Termux, Android SDK/Gradle setup is required and can be more fragile than Android Studio. Use a recent JDK 17 and Android SDK platform/build-tools 35.

## First run
1. Install the APK.
2. In the app, open Accessibility settings and enable "Magic Tiles Bot Auto Tap".
3. Grant overlay permission.
4. Tap Start Auto Play and accept screen-capture permission.
5. Open Magic Tiles 3 and test at a slower speed first.

## Limitations
This is a starter implementation, not a guaranteed perfect bot. The detector uses a basic dark-region heuristic and can tap incorrectly on menus, effects, different tile layouts, or changing screen dimensions. Test carefully. Android may stop capture when the user revokes permission or the system ends the projection. Only use where automation is allowed.
