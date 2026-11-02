#!/usr/bin/env bash
set -euo pipefail

# Build a static OpenSSL copy for every ABI configured in app/build.gradle.kts.
# OpenSSL is required because the original HTTrack engine's HTTPS transport is
# part of the real application, not an optional mock implementation.
OPENSSL_VERSION="3.4.2"
ROOT="${MIRROR_OPENSSL_ROOT:-$(pwd)/.android-openssl}"
NDK="${ANDROID_NDK_HOME:-${ANDROID_NDK_ROOT:-}}"
if [[ -z "$NDK" || ! -d "$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin" ]]; then
  echo "ANDROID_NDK_HOME must point to an installed Android NDK" >&2
  exit 2
fi
HOST_TAG="linux-x86_64"
TOOLCHAIN="$NDK/toolchains/llvm/prebuilt/$HOST_TAG/bin"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
curl -fsSL "https://www.openssl.org/source/openssl-${OPENSSL_VERSION}.tar.gz" -o "$WORK/openssl.tar.gz"
tar -xzf "$WORK/openssl.tar.gz" -C "$WORK"

build_one() {
  local abi="$1" platform="$2" cc="$3"
  local prefix="$ROOT/$abi"
  local src="$WORK/openssl-${OPENSSL_VERSION}"
  rm -rf "$prefix"
  mkdir -p "$prefix"
  cd "$src"
  make clean >/dev/null 2>&1 || true
  PATH="$TOOLCHAIN:$PATH" \
    ANDROID_NDK_ROOT="$NDK" \
    CC="$TOOLCHAIN/$cc" \
    AR="$TOOLCHAIN/llvm-ar" \
    RANLIB="$TOOLCHAIN/llvm-ranlib" \
    ./Configure "$platform" -D__ANDROID_API__=26 no-shared no-tests no-engine --prefix="$prefix"
  PATH="$TOOLCHAIN:$PATH" make -j"$(getconf _NPROCESSORS_ONLN)"
  PATH="$TOOLCHAIN:$PATH" make install_sw
}

build_one arm64-v8a android-arm64 aarch64-linux-android26-clang
build_one armeabi-v7a android-arm armv7a-linux-androideabi26-clang
build_one x86_64 android-x86_64 x86_64-linux-android26-clang
printf 'Android OpenSSL prefixes ready at %s\n' "$ROOT"
