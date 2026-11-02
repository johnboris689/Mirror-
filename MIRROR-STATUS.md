# Mirror — native integration and build verification status

This repository is based on the supplied HTTrack 3.51.0 source archive. The original C/autotools tree, tests, and `src/coucal` Git submodule declaration are retained. The supplied ZIP does not contain the submodule's checked-out files.

## Work completed in this iteration

- Added an Android JNI bridge that calls the upstream `hts_init()` and `hts_main()` public API. It passes the requested URL and app-private output directory to the real engine; it does not simulate a mirror.
- Added an Android NDK CMake target using the original HTTrack engine source files, including the original Coucal submodule source.
- Added Android ABI configuration for `arm64-v8a`, `armeabi-v7a`, and `x86_64`.
- Added a reproducible OpenSSL Android cross-build script. OpenSSL is required so HTTPS mirroring is not silently disabled.
- Updated the Android screen to invoke the JNI engine on a worker thread and report the real engine exit code/output location.
- Updated Android GitHub Actions to fetch submodules recursively, build OpenSSL for the configured ABIs, and build/upload a debug APK.

## Verification results — important

**No successful native build is claimed.** This execution environment could not complete the requested verification:

1. The supplied archive has an empty `src/coucal/`. Fetching `https://github.com/xroche/coucal.git` failed because this environment could not resolve `github.com`. The original engine explicitly requires that submodule; no substitute source was invented.
2. `./bootstrap` was attempted. Autoreconf stopped with `possibly undefined macro: AC_MSG_ERROR` in the supplied upstream `configure.ac`. No successful configure, compile, or `make check` result was obtained.
3. Android SDK/NDK and Gradle are not installed in this execution environment. The JNI/CMake/Gradle integration and OpenSSL cross-build script therefore remain unverified here. The Android CI workflow is configured to perform those builds on GitHub Actions, but has not run in this session.
4. The app currently runs the engine on an app-owned worker thread. A user should keep the app open during a mirror; a persistent foreground service/WorkManager job and interruption/resume UI still need device-level validation before Play Store release.

## Required next verification

On a full Git clone, initialize the upstream submodule and build the Linux project:

```sh
git submodule update --init --recursive
./bootstrap
./configure --disable-dependency-tracking
make -j"$(nproc)"
make check
```

For Android, open `android/` in Android Studio with SDK 35, NDK `27.2.12479018`, and CMake `3.22.1`; build the ABI-matched OpenSSL libraries using `android/scripts/build-openssl.sh`, then run `gradle assembleDebug`. The CI workflow performs the same dependency setup on GitHub Actions. Release APK/AAB signing must be configured locally with a private keystore; no credentials are included.

## Legal and attribution

The original `COPYING`, source copyright notices, and GPLv3-or-later terms remain. This rebrand does not transfer copyright in HTTrack or remove upstream attribution. Review trademark permissions before distribution under the name “Mirror”.
