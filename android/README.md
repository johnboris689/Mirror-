# Mirror for Android

The Android app calls the original HTTrack 3.51.0 engine through JNI. The JNI entry point calls `hts_init()` and `hts_main()`; it does not implement a separate crawler.

## Required native dependencies

- Android SDK platform 35
- Android NDK `27.2.12479018`
- CMake `3.22.1`
- The `src/coucal` Git submodule (initialize recursively)
- Static OpenSSL builds for each Android ABI, because HTTPS is part of the HTTrack engine

Build OpenSSL from the repository root:

```sh
export ANDROID_NDK_HOME="$ANDROID_HOME/ndk/27.2.12479018"
export MIRROR_OPENSSL_ROOT="$PWD/.android-openssl"
./android/scripts/build-openssl.sh
```

Then build the debug APK:

```sh
cd android
gradle assembleDebug
```

The CMake target intentionally fails when Coucal or ABI-matched OpenSSL is absent. It must not silently build a non-HTTPS or stub engine. The Android build has not yet been validated on a real device/emulator in this environment.

## Runtime notes

The current JNI call runs on a background executor and stores mirror output under the app's private files directory. Keep the app open while a mirror is running. Persistent foreground execution, pause/resume controls, and Android storage export still require device-level implementation and validation before Play Store release.

## Signing

Configure a private release keystore in your local Gradle signing configuration. Never commit the keystore or passwords. Debug APK builds do not establish Play Store readiness; release AAB signing, privacy disclosures, data-safety declarations, and Play policy review remain separate steps.
