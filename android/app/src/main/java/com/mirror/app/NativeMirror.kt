package com.mirror.app

/** JNI bridge to the original libhttrack engine. */
object NativeMirror {
    init { System.loadLibrary("httrack_engine") }
    external fun run(url: String, outputDirectory: String): Int
}
