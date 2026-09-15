# Hello World Android

Minimal Java-only Android app built with Gradle.

## Requirements

- Java 27
- Android SDK platform 35
- Android build tools 34.0.0 or newer

Gradle 9.7.1 is pinned by the wrapper. Gradle 9.7.1 does not yet support
running its daemon on Java 27; use a supported JDK for the Gradle runtime if
your local installation reports `Unsupported class file major version 71`.

## Build

```bash
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

For a release build:

```bash
./gradlew assembleRelease
```